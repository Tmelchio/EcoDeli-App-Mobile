package com.ecodeli.network

import com.ecodeli.models.api.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTHENTIFICATION ====================
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/valid")
    suspend fun validateToken(): Response<Map<String, UserInfo>>

    // ==================== UTILISATEURS ====================
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserInfo>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: Map<String, Any>): Response<UserInfo>

    // ==================== COMMANDES ====================
    @GET("commandes")
    suspend fun getCommandes(): Response<List<CommandeResponse>>

    @GET("commandes/{id}")
    suspend fun getCommande(@Path("id") commandeId: Int): Response<CommandeResponse>

    @POST("commandes")
    suspend fun createCommande(@Body request: CommandeRequest): Response<CommandeResponse>

    @POST("commandes/{id}/validate")
    suspend fun validateCommande(@Path("id") commandeId: Int): Response<ValidationResponse>

    // ==================== PRESTATIONS ====================
    @GET("prestations")
    suspend fun getPrestations(): Response<List<PrestationResponse>>

    @GET("prestations/{id}")
    suspend fun getPrestation(@Path("id") prestationId: Int): Response<PrestationResponse>

    @POST("prestations")
    suspend fun createPrestation(@Body request: PrestationRequest): Response<PrestationResponse>

    @POST("prestations/{id}/cancel")
    suspend fun cancelPrestation(@Path("id") prestationId: Int): Response<ValidationResponse>
}