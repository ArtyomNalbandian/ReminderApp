package com.example.reminder.di


import android.util.Log
import com.example.reminder.data.api.ReminderApi
import com.example.reminder.data.model.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TAG = "NetworkModule"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Используем IP вашего компьютера для тестирования на реальном устройстве
    private const val BASE_URL = "http://192.168.31.146:8000/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(LocalDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP Request: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d(TAG, "Sending request: ${request.url}")
                try {
                    val response = chain.proceed(request)
                    Log.d(TAG, "Received response: ${response.code} for ${request.url}")
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Error during request to ${request.url}", e)
                    throw e
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        Log.d(TAG, "Creating Retrofit instance with baseUrl: $BASE_URL")

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideReminderApi(retrofit: Retrofit): ReminderApi =
        retrofit.create(ReminderApi::class.java)
}

//    @Provides
//    @Singleton
//    fun provideMoshi(): Moshi = Moshi.Builder()
//        .add(LocalDateTimeAdapter())
//        .add(KotlinJsonAdapterFactory())
//        .build()
//
//
//    fun provideOkHttpClient(): OkHttpClient {
//        val loggingInterceptor = HttpLoggingInterceptor { message ->
//            Log.d(TAG, "HTTP Request: $message")
//        }.apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        return OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
//        Log.d(TAG, "Creating Retrofit instance with baseUrl: http://192.168.31.146:8000/")
//
//        return Retrofit.Builder()
//            .baseUrl("http://192.168.31.146:8000/")
//            .client(okHttpClient)
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .build()
//    }

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(): OkHttpClient {
//        val loggingInterceptor = HttpLoggingInterceptor { message ->
//            Log.d(TAG, "HTTP Request: $message")
//        }.apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        return OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
//        val baseUrl = "http://192.168.31.146:8000/"
//        Log.d(TAG, "Creating Retrofit instance with baseUrl: $baseUrl")
//
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(okHttpClient)
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .build()
//    }

//    @Provides
//    @Singleton
//    fun provideReminderApi(retrofit: Retrofit): ReminderApi =
//        retrofit.create(ReminderApi::class.java)
//}

//import com.example.reminder.update.data.api.ReminderApi
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    @Provides
//    @Singleton
//    fun provideMoshi(): Moshi = Moshi.Builder()
//        .add(KotlinJsonAdapterFactory())
//        .build()
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
////        .baseUrl("http://10.0.2.2:8000/") // Android эмулятор использует 10.0.2.2 для доступа к localhost
//        .baseUrl("http://192.168.31.146:8000/") // Android эмулятор использует 10.0.2.2 для доступа к localhost
//        .addConverterFactory(MoshiConverterFactory.create(moshi))
//        .build()
//
//    @Provides
//    @Singleton
//    fun provideReminderApi(retrofit: Retrofit): ReminderApi =
//        retrofit.create(ReminderApi::class.java)
//}