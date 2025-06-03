package com.example.reminder.update.data.repository

import com.example.reminder.update.data.api.ReminderApi
import com.example.reminder.update.data.model.Reminder
import javax.inject.Inject

class ReminderRepository @Inject constructor(
    private val api: ReminderApi
) {
    suspend fun getReminders(): List<Reminder> = api.getReminders()

    suspend fun getReminder(id: Int): Reminder = api.getReminder(id)

    suspend fun createReminder(reminder: Reminder): Reminder = api.createReminder(reminder)

    suspend fun deleteReminder(id: Int) = api.deleteReminder(id)
}