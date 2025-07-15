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

    // ==================== PRODUITS ====================
    @GET("products")
    suspend fun getProducts(): Response<List<ProductResponse>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") productId: Int): Response<ProductResponse>

    // Création d'un produit (en tant que vendeur)
    @POST("products")
    suspend fun createProduct(@Body request: ProductRequest): Response<ProductResponse>

    // ==================== DEMANDES DE PRODUITS (commandes) ====================
    @GET("products/requests")
    suspend fun getProductRequests(): Response<List<ProductRequestResponse>>

    @GET("products/requests/{id}")
    suspend fun getProductRequest(@Path("id") requestId: Int): Response<ProductRequestResponse>

    // Acheter un produit existant (créer une demande)
    @POST("products/{id}/buy")
    suspend fun buyProduct(@Path("id") productId: Int, @Body request: ProductRequestRequest): Response<ProductRequestResponse>

    // ==================== SERVICES (prestations) ====================
    @GET("services")
    suspend fun getServices(): Response<List<ServiceResponse>>

    @GET("services/{id}")
    suspend fun getService(@Path("id") serviceId: Int): Response<ServiceResponse>

    @POST("services")
    suspend fun createService(@Body request: ServiceRequest): Response<ServiceResponse>

    // ==================== LOCATIONS ====================
    @GET("locations")
    suspend fun getLocations(): Response<List<LocationInfo>>

    @POST("locations")
    suspend fun createLocation(@Body request: LocationRequest): Response<LocationInfo>
}