package com.example.reminder.data.api

import com.example.reminder.data.model.FcmToken
import com.example.reminder.data.model.Reminder
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReminderApi {

    @GET("reminders")
    suspend fun getReminders(): List<Reminder>

    @GET("reminders/{id}")
    suspend fun getReminder(@Path("id") id: Int): Reminder

    @POST("reminders")
    suspend fun createReminder(@Body reminder: Reminder): Reminder

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: Int)

    @POST("fcm/token/")
    suspend fun updateFcmToken(@Body fmcToken: FcmToken)
}