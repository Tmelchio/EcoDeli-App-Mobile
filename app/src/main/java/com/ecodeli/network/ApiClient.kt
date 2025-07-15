package com.ecodeli.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = getStoredToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader(ApiConfig.AUTHORIZATION_HEADER, "${ApiConfig.BEARER_PREFIX}$token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun getStoredToken(): String? {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    fun saveToken(token: String) {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
    }

    fun clearToken() {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()
    }
}