package com.example.reminder.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminder.data.model.Reminder
import com.example.reminder.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "HomeViewModel"

enum class ReminderFilter {
    ACTIVE,
    HISTORY,
    TRASH
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    private val _currentFilter = MutableStateFlow(ReminderFilter.ACTIVE)
    val currentFilter: StateFlow<ReminderFilter> = _currentFilter.asStateFlow()

    val reminders: StateFlow<List<Reminder>> = combine(
        _reminders,
        _currentFilter
    ) { reminders, filter ->
        val now = LocalDateTime.now()
        
        val filteredReminders = when (filter) {
            // Активные: статус pending и время уведомления в будущем
            ReminderFilter.ACTIVE -> reminders.filter {
                it.status == "pending" && it.notificationTime.isAfter(now)
            }
            // История: статус completed/sent или (pending и время уведомления в прошлом)
            ReminderFilter.HISTORY -> reminders.filter {
                it.status == "completed" || 
                it.status == "sent" ||
                (it.status == "pending" && it.notificationTime.isBefore(now))
            }
            // Корзина: статус cancelled
            ReminderFilter.TRASH -> reminders.filter {
                it.status == "cancelled"
            }
        }

        // Сортируем по времени уведомления (новые сверху)
        filteredReminders.sortedByDescending { it.notificationTime }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var autoUpdateJob: Job? = null

    init {
        loadReminders()
    }

    fun setFilter(filter: ReminderFilter) {
        _currentFilter.value = filter
    }

    fun startAutoUpdate() {
        stopAutoUpdate()
        autoUpdateJob = viewModelScope.launch {
            while (isActive) {
                loadRemindersQuietly()
                delay(5000)
            }
        }
    }

    fun stopAutoUpdate() {
        autoUpdateJob?.cancel()
        autoUpdateJob = null
    }

    // Тихое обновление списка без индикатора загрузки
    private suspend fun loadRemindersQuietly() {
        try {
            val remindersList = repository.getReminders()
            _reminders.value = remindersList
            Log.d(TAG, "Тихое обновление: загружено ${remindersList.size} напоминаний")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при тихом обновлении напоминаний", e)
            _error.value = e.message ?: "Неизвестная ошибка"
        }
    }

    // Обновление списка с индикатором загрузки (для ручного обновления)
    fun loadReminders() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                _error.value = null
                val remindersList = repository.getReminders()
                _reminders.value = remindersList
                Log.d(TAG, "Загружено ${remindersList.size} напоминаний")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке напоминаний", e)
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.deleteReminder(id)
                Log.d(TAG, "Напоминание $id удалено")
                loadReminders()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении напоминания $id", e)
                _error.value = e.message ?: "Неизвестная ошибка"
                throw e
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoUpdate()
    }
}