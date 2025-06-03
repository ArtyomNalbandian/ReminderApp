package com.example.reminder.update.data.model

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class Reminder(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "text") val text: String,
    @Json(name = "notification_time") val notificationTime: LocalDateTime,
    @Json(name = "notification_type") val notificationType: NotificationType,
    @Json(name = "recipient") val recipient: String,
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: LocalDateTime? = null,
)

enum class NotificationType {
    @Json(name = "fcm")
    FCM,
    @Json(name = "email")
    EMAIL
}
private const val TAG = "FCMService"

class LocalDateTimeAdapter {
    @ToJson
    fun toJson(value: LocalDateTime): String {
        // Преобразуем локальное время в UTC и форматируем в ISO формат
        val utcTime = value.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime()
        val result = utcTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        Log.d(TAG, "Converting to JSON: $value -> $result")
        return result
    }

    @FromJson
    fun fromJson(value: String): LocalDateTime {
        Log.d(TAG, "Converting from JSON: $value")
        return try {
            // Парсим время и считаем его UTC
            val utcTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // Конвертируем в локальное время
            val result = utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
            Log.d(TAG, "Converted to LocalDateTime: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing datetime: $value", e)
            // Если не удалось распарсить, возвращаем как есть
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}