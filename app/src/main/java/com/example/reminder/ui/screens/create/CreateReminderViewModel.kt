package com.example.reminder.ui.screens.create


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminder.data.model.NotificationType
import com.example.reminder.data.model.Reminder
import com.example.reminder.data.repository.ReminderRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "CreateReminderViewModel"

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun createReminder(
        text: String,
        notificationTime: LocalDateTime,
        notificationType: NotificationType,
        email: String? = null,
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _isSuccess.value = false

//                Log.d(TAG, "Creating reminder: text=$text, time=$notificationTime, type=$notificationType")
                // Получаем FCM токен, если тип уведомления FCM
                val recipient = if (notificationType == NotificationType.FCM) {
                    try {
                        FirebaseMessaging.getInstance().token.await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при получении FCM токена", e)
                        _error.value = "Не удалось получить FCM токен"
                        return@launch
                    }
                } else {
                    "$email" // Для email уведомлений recipient будет задаваться на экране
                }

                Log.d(TAG, "Creating reminder: text=$text, time=$notificationTime, type=$notificationType, recipient=$recipient")
                val reminder = Reminder(
                    text = text,
                    notificationTime = notificationTime,
                    notificationType = notificationType,
                    recipient = recipient,
//                    recipient = "", // FCM token будет добавлен автоматически на сервере
                    status = "pending"
                )
                repository.createReminder(reminder)
                Log.d(TAG, "Reminder created successfully")
                _isSuccess.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error creating reminder", e)
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.reminder.update.data.model.NotificationType
//import com.example.reminder.update.data.model.Reminder
//import com.example.reminder.update.data.repository.ReminderRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import java.time.LocalDateTime
//import javax.inject.Inject
//
//@HiltViewModel
//class CreateReminderViewModel @Inject constructor(
//    private val repository: ReminderRepository
//) : ViewModel() {
//
//    fun createReminder(
//        text: String,
//        notificationTime: LocalDateTime,
//        notificationType: NotificationType
//    ) {
//        viewModelScope.launch {
//            try {
//                val reminder = Reminder(
//                    text = text,
//                    notificationTime = notificationTime,
//                    notificationType = notificationType,
//                    recipient = "", // FCM token будет добавлен автоматически на сервере
//                    status = "pending"
//                )
//                repository.createReminder(reminder)
//                Log.d("FCMService", "reminder created - $reminder")
//            } catch (e: Exception) {
//                Log.d("FCMService", "error - $e")
//                // Здесь можно добавить обработку ошибок
//            }
//        }
//    }
//}