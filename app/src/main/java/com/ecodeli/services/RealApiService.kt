package com.ecodeli.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ecodeli.models.api.*
import com.ecodeli.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealApiService(private val context: Context) {

    private val apiClient = ApiClient(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "RealApiService"
    }

    // Connexion
    suspend fun login(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val request = LoginRequest(email, password)
                val response = apiClient.apiService.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            apiClient.saveToken(loginResponse.token)
                            saveUserInfo(loginResponse.user)
                            callback(true, "client", "Connexion réussie")
                        } else {
                            callback(false, null, "Réponse vide du serveur")
                        }
                    } else {
                        val errorMsg = if (response.code() == 400) {
                            "Email ou mot de passe incorrect"
                        } else {
                            "Erreur de connexion (${response.code()})"
                        }
                        callback(false, null, errorMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de connexion", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // Inscription
    suspend fun register(userData: Map<String, String>, callback: (Boolean, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val request = RegisterRequest(
                    email = userData["email"] ?: "",
                    password = userData["password"] ?: "",
                    firstname = userData["prenom"] ?: "",
                    name = userData["nom"] ?: "",
                    birthday = userData["birthDate"] ?: ""
                )

                val response = apiClient.apiService.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        callback(true, "Inscription réussie")
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Un compte avec cet email existe déjà"
                            else -> "Erreur d'inscription (${response.code()})"
                        }
                        callback(false, errorMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur d'inscription", e)
            withContext(Dispatchers.Main) {
                callback(false, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // Validation token
    suspend fun validateToken(callback: (Boolean, UserInfo?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.validateToken()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val userMap = response.body()
                        val user = userMap?.get("user")
                        if (user != null) {
                            saveUserInfo(user)
                            callback(true, user)
                        } else {
                            callback(false, null)
                        }
                    } else {
                        callback(false, null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de validation token", e)
            withContext(Dispatchers.Main) {
                callback(false, null)
            }
        }
    }

    // Déconnexion
    suspend fun logout(callback: (Boolean, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                apiClient.apiService.logout()

                withContext(Dispatchers.Main) {
                    apiClient.clearToken()
                    clearUserInfo()
                    callback(true, "Déconnexion réussie")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de déconnexion", e)
            withContext(Dispatchers.Main) {
                apiClient.clearToken()
                clearUserInfo()
                callback(true, "Déconnexion locale")
            }
        }
    }

    // Sauvegarde info utilisateur
    private fun saveUserInfo(user: UserInfo) {
        prefs.edit().apply {
            putString("user_id", user._id.toString())
            putString("user_email", user.email)
            putString("user_firstname", user.firstname)
            putString("user_name", user.name)
            putString("user_description", user.description ?: "")
            putString("user_join_date", user.join_date)
            putString("user_role", user.role.name)

            user.subscription?.let { sub ->
                putString("user_subscription", sub.name)
                putFloat("user_subscription_price", sub.price.toFloat())
            }

            apply()
        }
    }

    // Effacer info utilisateur
    private fun clearUserInfo() {
        prefs.edit().clear().apply()
    }
}