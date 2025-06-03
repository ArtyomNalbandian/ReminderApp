//package com.example.reminder
//
//import com.google.gson.annotations.SerializedName
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//
//interface ReminderApi {
//
//    @POST("/reminders")
//    suspend fun createReminder(@Body reminder: ReminderCreate): ReminderResponse
//
//    @GET("/reminders")
//    suspend fun getReminders(): List<ReminderResponse>
//}
//
//data class ReminderCreate(
//    val title: String,
//    val description: String,
//    @SerializedName("date_time") // это короче для того чтобы название совпало с питоновским
//    val dateTime: String,
//    @SerializedName("notification_type") // аналогично
//    val notificationType: String,
//    @SerializedName("user_email")
//    val userEmail: String,
//    @SerializedName("device_token")
//    val deviceToken: String,
//)
//
//data class ReminderResponse(
//    val id: Int,
//    val title: String,
//    val description: String,
//    val dateType: String,
//    val notificationType: String,
//)
//
//object RetrofitInstance {
//    val api: ReminderApi by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://reminder-qqvg.onrender.com")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ReminderApi::class.java)
//    }
//}