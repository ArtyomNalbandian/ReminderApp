package com.example.reminder.update.ui.screens.home


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reminder.update.data.model.Reminder
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateReminder: () -> Unit,
    highlightedReminderId: Int? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Запускаем автоматическое обновление при активации экрана
    LaunchedEffect(Unit) {
        viewModel.startAutoUpdate()
    }

    // Останавливаем автоматическое обновление при уничтожении экрана
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAutoUpdate()
        }
    }

    // Эффект для автоматического выделения напоминания при переходе из уведомления
    LaunchedEffect(highlightedReminderId) {
        if (highlightedReminderId != null) {
            val reminder = reminders.find { it.id == highlightedReminderId }
            if (reminder != null) {
                selectedReminder = reminder
                // Автоматически закрываем диалог через 3 секунды
                coroutineScope.launch {
                    delay(3000)
                    if (selectedReminder?.id == highlightedReminderId) {
                        selectedReminder = null
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Напоминания") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(
                    selectedTabIndex = currentFilter.ordinal,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Tab(
                        selected = currentFilter == ReminderFilter.ACTIVE,
                        onClick = { viewModel.setFilter(ReminderFilter.ACTIVE) },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        text = { Text("Активные") }
                    )
                    Tab(
                        selected = currentFilter == ReminderFilter.HISTORY,
                        onClick = { viewModel.setFilter(ReminderFilter.HISTORY) },
                        icon = { Icon(Icons.Default.Done, contentDescription = null) },
                        text = { Text("История") }
                    )
                    Tab(
                        selected = currentFilter == ReminderFilter.TRASH,
                        onClick = { viewModel.setFilter(ReminderFilter.TRASH) },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { Text("Корзина") }
                    )
                }
            }
        },
        floatingActionButton = {
            // Показываем FAB только на экране активных напоминаний
            if (currentFilter == ReminderFilter.ACTIVE) {
                FloatingActionButton(
                    onClick = onCreateReminder,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать напоминание")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            PullToRefreshBox(
                state = rememberPullToRefreshState(),
                onRefresh = { viewModel.loadReminders() },
                isRefreshing = isRefreshing,
            ) {
                if (reminders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentFilter) {
                                ReminderFilter.ACTIVE -> "Нет активных напоминаний"
                                ReminderFilter.HISTORY -> "История пуста"
                                ReminderFilter.TRASH -> "Корзина пуста"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reminders) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                isHighlighted = reminder.id == highlightedReminderId,
                                onClick = { selectedReminder = reminder },
                                onDeleteClick = { reminderToDelete = reminder }
                            )
                        }
                    }
                }
            }

            // Dialog для отображения деталей напоминания
            selectedReminder?.let { reminder ->
                AlertDialog(
                    onDismissRequest = { selectedReminder = null },
                    title = { Text("Детали напоминания") },
                    text = {
                        Column {
                            Text("Время: ${reminder.notificationTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Текст: ${reminder.text}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Тип уведомления: ${reminder.notificationType}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Статус: ${reminder.status}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedReminder = null }) {
                            Text("Закрыть")
                        }
                    }
                )
            }

            // Dialog подтверждения удаления
            reminderToDelete?.let { reminder ->
                AlertDialog(
                    onDismissRequest = { reminderToDelete = null },
                    title = { Text("Удаление напоминания") },
                    text = { Text("Вы уверены, что хотите удалить это напоминание?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        viewModel.deleteReminder(reminder.id)
                                        snackbarHostState.showSnackbar("Напоминание удалено")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Ошибка при удалении: ${e.message}")
                                    }
                                }
                                reminderToDelete = null
                            }
                        ) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { reminderToDelete = null }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItem(
    reminder: Reminder,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else when (reminder.status) {
                "completed", "sent" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                "cancelled" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = when (reminder.status) {
                        "completed", "sent" -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        "cancelled" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Время: ${reminder.notificationTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (reminder.status) {
                        "completed", "sent" -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        "cancelled" -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (reminder.status) {
                            "completed" -> "Выполнено"
                            "sent" -> "Отправлено"
                            "cancelled" -> "Отменено"
                            "pending" -> "Ожидает"
                            else -> reminder.status
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (reminder.status) {
                            "completed", "sent" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            "cancelled" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить напоминание",
                    tint = MaterialTheme.colorScheme.error.copy(
                        alpha = when (reminder.status) {
                            "completed", "sent" -> 0.7f
                            "cancelled" -> 0.5f
                            else -> 1f
                        }
                    )
                )
            }
        }
    }
}