package com.ecodeli.models.api

// Connexion
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserInfo
)

// Inscription
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstname: String,
    val name: String,
    val birthday: String
)

data class RegisterResponse(
    val message: String,
    val userId: Int
)

// Utilisateur - CORRIGÉ pour gérer les différents formats
data class UserInfo(
    val _id: Int,
    val firstname: String,
    val name: String,
    val image: String?,
    val email: String,
    val description: String?,
    val join_date: String,
    val role: Any, // Peut être un objet RoleInfo ou un simple ID
    val subscription: Any? = null // Peut être un objet SubscriptionInfo ou un simple ID
)

// Rôle complet
data class RoleInfo(
    val _id: Int,
    val name: String,
    val access_level: Int
)

// Abonnement complet
data class SubscriptionInfo(
    val _id: Int,
    val name: String,
    val color: String,
    val price: Double,
    val assurance_max: Double,
    val delivery_reduction: Double,
    val permanent_reduction: Double
)

// Erreur API
data class ApiError(
    val error: String
)

// Réponse de validation de token
data class TokenValidationResponse(
    val user: UserInfo
)