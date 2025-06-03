package com.example.reminder.update.ui.screens.create


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reminder.update.data.model.NotificationType
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateReminderViewModel = hiltViewModel()
) {
    var text by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedNotificationType by remember { mutableStateOf(NotificationType.FCM) }
    var email by remember { mutableStateOf("") }

    val calendarState = rememberUseCaseState()
    val clockState = rememberUseCaseState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Обработка успешного создания
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onNavigateBack()
        }
    }

    // Обработка ошибок
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать напоминание") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Текст напоминания") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Выбор даты
                OutlinedButton(
                    onClick = { calendarState.show() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(
                        selectedDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            ?: "Выберите дату"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Выбор времени
                OutlinedButton(
                    onClick = { clockState.show() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(
                        selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                            ?: "Выберите время"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Выбор типа уведомления
                Column {
                    Text(
                        "Тип уведомления",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotificationType.values().forEach { type ->
                            FilterChip(
                                selected = selectedNotificationType == type,
                                onClick = { selectedNotificationType = type },
                                label = { Text(type.name) },
                                enabled = !isLoading
                            )
                        }
                    }
                }

                // Поле для ввода email
                if (selectedNotificationType == NotificationType.EMAIL) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }
            }

            // Кнопка создания
            Button(
                onClick = {
                    if (selectedDate != null && selectedTime != null) {
                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        viewModel.createReminder(
                            text = text,
                            notificationTime = dateTime,
                            notificationType = selectedNotificationType,
                            email = if (selectedNotificationType == NotificationType.EMAIL) email else null
                        )
                    }
                },
                enabled = text.isNotBlank() &&
                        selectedDate != null &&
                        selectedTime != null &&
                        !isLoading &&
                        (selectedNotificationType != NotificationType.EMAIL || email.isNotBlank()),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Создать напоминание")
                }
            }
        }
    }

    CalendarDialog(
        state = calendarState,
        selection = CalendarSelection.Date { date ->
            selectedDate = date
        },
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true
        )
    )

    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { hours, minutes ->
            selectedTime = LocalTime.of(hours, minutes)
        },
        config = ClockConfig(
            is24HourFormat = true
        )
    )
}

//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.reminder.update.data.model.NotificationType
//import com.maxkeppeker.sheets.core.models.base.UseCaseState
//import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
//import com.maxkeppeler.sheets.calendar.CalendarDialog
//import com.maxkeppeler.sheets.calendar.models.CalendarConfig
//import com.maxkeppeler.sheets.calendar.models.CalendarSelection
//import com.maxkeppeler.sheets.clock.ClockDialog
//import com.maxkeppeler.sheets.clock.models.ClockConfig
//import com.maxkeppeler.sheets.clock.models.ClockSelection
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateReminderScreen(
//    onNavigateBack: () -> Unit,
//    viewModel: CreateReminderViewModel = hiltViewModel()
//) {
//    var text by remember { mutableStateOf("") }
//    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
//    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
//    var selectedNotificationType by remember { mutableStateOf(NotificationType.FCM) }
//
//    val calendarState = rememberUseCaseState()
//    val clockState = rememberUseCaseState()
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//    val isSuccess by viewModel.isSuccess.collectAsState()
//
//    // Обработка успешного создания
//    LaunchedEffect(isSuccess) {
//        if (isSuccess) {
//            onNavigateBack()
//        }
//    }
//
//    // Обработка ошибок
//    LaunchedEffect(error) {
//        error?.let {
//            scope.launch {
//                snackbarHostState.showSnackbar(
//                    message = it,
//                    duration = SnackbarDuration.Long
//                )
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Создать напоминание") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .padding(paddingValues)
//                .padding(16.dp)
//                .fillMaxSize()
//        ) {
//            Column(modifier = Modifier.fillMaxSize()) {
//                OutlinedTextField(
//                    value = text,
//                    onValueChange = { text = it },
//                    label = { Text("Текст напоминания") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !isLoading
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Выбор даты
//                OutlinedButton(
//                    onClick = { calendarState.show() },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !isLoading
//                ) {
//                    Text(
//                        selectedDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
//                            ?: "Выберите дату"
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Выбор времени
//                OutlinedButton(
//                    onClick = { clockState.show() },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !isLoading
//                ) {
//                    Text(
//                        selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
//                            ?: "Выберите время"
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Выбор типа уведомления
//                Column {
//                    Text(
//                        "Тип уведомления",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        NotificationType.values().forEach { type ->
//                            FilterChip(
//                                selected = selectedNotificationType == type,
//                                onClick = { selectedNotificationType = type },
//                                label = { Text(type.name) },
//                                enabled = !isLoading
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Кнопка создания
//            Button(
//                onClick = {
//                    if (selectedDate != null && selectedTime != null) {
//                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
//                        viewModel.createReminder(
//                            text = text,
//                            notificationTime = dateTime,
//                            notificationType = selectedNotificationType
//                        )
//                    }
//                },
//                enabled = text.isNotBlank() && selectedDate != null && selectedTime != null && !isLoading,
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//            ) {
//                if (isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                } else {
//                    Text("Создать напоминание")
//                }
//            }
//        }
//    }
//
//    CalendarDialog(
//        state = calendarState,
//        selection = CalendarSelection.Date { date ->
//            selectedDate = date
//        },
//        config = CalendarConfig(
//            monthSelection = true,
//            yearSelection = true
//        )
//    )
//
//    ClockDialog(
//        state = clockState,
//        selection = ClockSelection.HoursMinutes { hours, minutes ->
//            selectedTime = LocalTime.of(hours, minutes)
//        },
//        config = ClockConfig(
//            is24HourFormat = true
//        )
//    )
//}
