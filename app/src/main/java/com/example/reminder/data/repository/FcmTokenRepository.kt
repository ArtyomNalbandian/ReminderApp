package com.example.reminder.data.repository

import android.util.Log
import com.example.reminder.data.api.ReminderApi
import com.example.reminder.data.model.FcmToken
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FCMService"

@Singleton
class FcmTokenRepository @Inject constructor(
    private val api: ReminderApi
) {
    suspend fun updateToken(token: String) {
        try {
            Log.d(TAG, "Отправка FCM токена на сервер: $token")
            api.updateFcmToken(FcmToken(token))
            Log.d(TAG, "FCM токен успешно отправлен на сервер")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отправке FCM токена на сервер", e)
            throw e
        }
    }
}