package com.example.reminder.update.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.reminder.MainActivity
import com.example.reminder.R
import com.example.reminder.update.data.repository.FcmTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FCMService"

@AndroidEntryPoint
class ReminderFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var fcmTokenRepository: FcmTokenRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Получен новый FCM токен: $token")
        serviceScope.launch {
            try {
                fcmTokenRepository.updateToken(token)
                Log.d(TAG, "FCM токен успешно отправлен на сервер")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при отправке FCM токена на сервер", e)
                // Обработка ошибки отправки токена
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Получено FCM сообщение: ${message.data}")

        // Создаем канал уведомлений для Android 8.0 (API level 26) и выше
        createNotificationChannel()

        // Получаем данные из сообщения
        val title = message.data["title"] ?: "Напоминание"
        val text = message.data["text"] ?: "Пришло время для вашего напоминания"
        val reminderId = message.data["reminderId"]?.toIntOrNull()

        Log.d(TAG, "Создаем уведомление: title=$title, text=$text, reminderId=$reminderId")
        // Создаем intent для открытия приложения при нажатии на уведомление
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            reminderId?.let {
                putExtra("reminder_id", it)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Показываем уведомление
//        notificationManager.notify(reminderId ?: System.currentTimeMillis().toInt(), notification)
        try {
            notificationManager.notify(reminderId ?: System.currentTimeMillis().toInt(), notification)
            Log.d(TAG, "Уведомление успешно показано")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при показе уведомления", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminders"
            val descriptionText = "Notifications for reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
//            notificationManager.createNotificationChannel(channel)
            try {
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Канал уведомлений успешно создан")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при создании канала уведомлений", e)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "reminders_channel"
    }
}