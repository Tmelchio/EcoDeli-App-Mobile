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

    @GET("users")
    suspend fun getUsers(): Response<List<UserInfo>>

    // ==================== PRODUITS ====================
    @GET("products")
    suspend fun getProducts(): Response<List<ProductResponse>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") productId: Int): Response<ProductResponse>

    // Création d'un produit (en tant que vendeur)
    @POST("products")
    suspend fun createProduct(@Body request: ProductRequest): Response<ProductResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") productId: Int, @Body request: ProductRequest): Response<ProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): Response<Unit>

    // ==================== DEMANDES DE PRODUITS (commandes) ====================
    @GET("products/requests")
    suspend fun getProductRequests(): Response<List<ProductRequestResponse>>

    @GET("products/requests/{id}")
    suspend fun getProductRequest(@Path("id") requestId: Int): Response<ProductRequestResponse>

    @GET("products/requests/unassigned")
    suspend fun getUnassignedProductRequests(): Response<List<ProductRequestResponse>>

    // Acheter un produit existant (créer une demande)
    @POST("products/{id}/buy")
    suspend fun buyProduct(@Path("id") productId: Int, @Body request: ProductRequestRequest): Response<ProductRequestResponse>

    @PUT("products/requests/{id}")
    suspend fun updateProductRequest(@Path("id") requestId: Int, @Body request: Map<String, Any>): Response<ProductRequestResponse>

    @DELETE("products/requests/{id}")
    suspend fun deleteProductRequest(@Path("id") requestId: Int): Response<Unit>

    // ==================== SERVICES (prestations) ====================
    @GET("services")
    suspend fun getServices(): Response<List<ServiceResponse>>

    @GET("services/{id}")
    suspend fun getService(@Path("id") serviceId: Int): Response<ServiceResponse>

    @POST("services")
    suspend fun createService(@Body request: ServiceRequest): Response<ServiceResponse>

    @PUT("services/{id}")
    suspend fun updateService(@Path("id") serviceId: Int, @Body request: ServiceRequest): Response<ServiceResponse>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") serviceId: Int): Response<Unit>

    // ==================== LOCATIONS ====================
    @GET("locations")
    suspend fun getLocations(): Response<List<LocationInfo>>

    @GET("locations/{id}")
    suspend fun getLocation(@Path("id") locationId: Int): Response<LocationInfo>

    @POST("locations")
    suspend fun createLocation(@Body request: LocationRequest): Response<LocationInfo>

    @PUT("locations/{id}")
    suspend fun updateLocation(@Path("id") locationId: Int, @Body request: LocationRequest): Response<LocationInfo>

    @DELETE("locations/{id}")
    suspend fun deleteLocation(@Path("id") locationId: Int): Response<Unit>

    // ==================== LIVRAISONS ====================
    @GET("deliveries")
    suspend fun getDeliveries(): Response<List<DeliveryInfo>>

    @GET("deliveries/{id}")
    suspend fun getDelivery(@Path("id") deliveryId: Int): Response<DeliveryInfo>

    @POST("deliveries")
    suspend fun createDelivery(@Body request: Map<String, Any>): Response<DeliveryInfo>

    @PUT("deliveries/{id}")
    suspend fun updateDelivery(@Path("id") deliveryId: Int, @Body request: Map<String, Any>): Response<DeliveryInfo>

    @DELETE("deliveries/{id}")
    suspend fun deleteDelivery(@Path("id") deliveryId: Int): Response<Unit>

    // ==================== NOTIFICATIONS ====================
    @GET("notifications")
    suspend fun getNotifications(): Response<List<Map<String, Any>>>

    @POST("notifications")
    suspend fun createNotification(@Body request: Map<String, Any>): Response<Map<String, Any>>

    @PUT("notifications/{id}")
    suspend fun updateNotification(@Path("id") notificationId: Int, @Body request: Map<String, Any>): Response<Map<String, Any>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: Int): Response<Unit>
}