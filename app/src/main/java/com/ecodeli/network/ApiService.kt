package com.ecodeli.network

import com.ecodeli.models.api.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentification
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/valid")
    suspend fun validateToken(): Response<Map<String, UserInfo>>

    // Utilisateurs
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserInfo>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: Map<String, Any>): Response<UserInfo>
}