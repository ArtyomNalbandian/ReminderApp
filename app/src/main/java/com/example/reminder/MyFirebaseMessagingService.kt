//package com.example.reminder
//
//import android.app.ActivityManager
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.os.Build
//import android.util.Log
//import androidx.core.app.ActivityCompat.requestPermissions
//import androidx.core.app.NotificationCompat
//import androidx.core.content.ContextCompat.getSystemService
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class FirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onNewToken(token: String) {
//        Log.d("FCM", "Refreshed token: $token")
//        // Отправьте этот токен на ваш сервер
//    }
//
//    override fun onMessageReceived(message: RemoteMessage) {
//        if (message.notification != null) {
//            Log.d("FCM", "Message Notification Body: ${message.notification?.body}")
//            // Только для фоновых уведомлений
//            if (!isAppInForeground()) {
//                sendNotification(
//                    message.notification?.title ?: "Новое уведомление",
//                    message.notification?.body ?: ""
//                )
//            }
//        }
//    }
//
//    private fun sendNotification(title: String, messageBody: String) {
//        // 1. Создаём уникальный requestCode для каждого уведомления
//        val requestCode = System.currentTimeMillis().toInt()
//
//        // 2. Создаём Intent с флагами
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            putExtra("notification_id", requestCode)
//        }
//
//        // 3. Создаём PendingIntent с FLAG_IMMUTABLE
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            requestCode,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        // 4. Создаём канал (обязательно для Android 8.0+)
//        val channelId = "reminder_channel_id"
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Напоминания",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Канал для важных напоминаний"
//                enableLights(true)
//                lightColor = Color.RED
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // 5. Строим уведомление
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle(title)
//            .setContentText(messageBody)
//            .setPriority(NotificationCompat.PRIORITY_MAX)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setOnlyAlertOnce(true)
//            .build()
//
//        // 6. Показываем уведомление
//        notificationManager.notify(requestCode, notification)
//    }
//}
//
//private fun isAppInForeground(): Boolean {
//    val appProcessInfo = ActivityManager.RunningAppProcessInfo()
//    ActivityManager.getMyMemoryState(appProcessInfo)
//    return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
//}