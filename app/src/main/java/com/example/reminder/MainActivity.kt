package com.example.reminder


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reminder.ui.theme.ReminderTheme
import com.example.reminder.update.ui.screens.create.CreateReminderScreen
import com.example.reminder.update.ui.screens.home.HomeScreen
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "FCMService"
// fOezvTp_T5iHyExF4urh3Y:APA91bEoJG0x5lJaKBl_S_QWq3dLuA5vizaXm7-GcpGHhIeGWJkUEbHw7lDqZltjYXg2ooMKdiwciRsBmn29xYSZa9J5Jb9A8fu_wPiukEC0uMESEG-6o4M

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Разрешение на уведомления получено")
            // Разрешение получено
        } else {
            Log.w(TAG, "Разрешение на уведомления не получено")
            Toast.makeText(this, "Для работы напоминаний необходимо разрешение на уведомления", Toast.LENGTH_LONG).show()
            // Разрешение не получено
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрашиваем разрешение на показ уведомлений для Android 13+
        askNotificationPermission()

        // Получаем FCM токен при запуске
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Не удалось получить FCM токен", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                // Токен будет автоматически отправлен на сервер через FCM сервис
//            }
//        }
                Log.d(TAG, "FCM токен: $token")
            })

        setContent {
            ReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (intent.hasExtra("reminder_id")) {
                        "reminder/${intent.getIntExtra("reminder_id", -1)}"
                    } else {
                        "home"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("home") {
                            HomeScreen(
                                onCreateReminder = {
                                    navController.navigate("create")
                                }
                            )
                        }
                        composable("create") {
                            CreateReminderScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("reminder/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                            HomeScreen(
                                onCreateReminder = {
                                    navController.navigate("create")
                                },
                                highlightedReminderId = id
                            )
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Разрешение уже есть
                    Log.d(TAG, "Разрешение на уведомления уже получено")

                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Показать объяснение, почему нужны уведомления
                    Log.d(TAG, "Показываем объяснение необходимости разрешения")
                    Toast.makeText(this, "Для работы напоминаний необходимо разрешение на уведомления", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.d(TAG, "Запрашиваем разрешение на уведомления")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

//
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.Manifest
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.reminder.ui.theme.ReminderTheme
//import com.google.firebase.messaging.FirebaseMessaging
//import kotlinx.coroutines.launch
//import retrofit2.HttpException
//
//class MainActivity : ComponentActivity() {
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (!isGranted) {
//            Log.d("testLog", "Разрешение не дано")
//        }
//    }
//    // dpOm09vfRPWAITvSwmFhqG:APA91bHzlwb4vk2Jwde3l4uvwOQ36CUrdKAX5FH4QTbiovPnc4cDnRRoUuVfRgiwiVmQTu7c7Z0wVVqgbaqxVP6qz1edAcMeJfguJi0rGj-PM7YDZ4mgq64
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ReminderApp()
//        }
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                Log.d("testLog", token)
//            }
//        }
//
//        // Проверка разрешений для Android 13+
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
////            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
////        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val requestPermissionLauncher = registerForActivityResult(
//                ActivityResultContracts.RequestPermission()
//            ) { isGranted ->
//                if (!isGranted) {
//                    Toast.makeText(this,
//                        "Уведомления не будут работать без разрешения!",
//                        Toast.LENGTH_LONG).show()
//                }
//            }
//            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//        }
//
//    }
//}
//
//
//@Composable
//fun ReminderApp() {
//    var title by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var reminders by remember { mutableStateOf<List<ReminderResponse>>(emptyList()) }
//    val scope = rememberCoroutineScope()
//
//    Column(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxSize()
//    ) {
//        OutlinedTextField(
//            value = title,
//            onValueChange = { title = it },
//            label = { Text("Название") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Описание") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = {
//                scope.launch {
//                    try {
//                        RetrofitInstance.api.createReminder(
//                            ReminderCreate(
//                                title = title,
//                                description = description,
//                                dateTime = "2025-05-05T23:40:00",  // Пока фиксированная дата, потом сделаю так чтобы вводилось пользователем, щас лень
//                                notificationType = "push", // что-нибудь придумаю лол
//                                userEmail = "randomEmailYo",
//                                deviceToken = "someToken",
//                            )
//                        )
//                        reminders = RetrofitInstance.api.getReminders()
//                    } catch (e: HttpException) {
//                        val errorBody = e.response()?.errorBody()?.string()
//                        Log.d("testLog", "422 error lol - $errorBody")
//                        e.printStackTrace()
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Создать напоминание")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(reminders) { reminder ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(text = reminder.title, style = MaterialTheme.typography.headlineSmall)
//                        Text(text = reminder.description)
//                        Text(text = "Тип: ${reminder.notificationType}")
//                    }
//                }
//            }
//        }
//    }
//}