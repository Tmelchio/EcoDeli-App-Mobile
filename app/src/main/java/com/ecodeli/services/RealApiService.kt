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

    // ==================== AUTHENTIFICATION ====================

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

    // ==================== COMMANDES ====================

    suspend fun getCommandes(callback: (Boolean, List<CommandeResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.getCommandes()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val commandes = response.body() ?: emptyList()
                        callback(true, commandes, null)
                    } else {
                        callback(false, null, "Erreur lors du chargement des commandes")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get commandes", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun createCommande(request: CommandeRequest, callback: (Boolean, CommandeResponse?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.createCommande(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val commande = response.body()
                        callback(true, commande, "Commande créée avec succès")
                    } else {
                        callback(false, null, "Erreur lors de la création de la commande")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur create commande", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun validateCommande(commandeId: Int, callback: (Boolean, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.validateCommande(commandeId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        callback(true, "Commande validée avec succès")
                    } else {
                        callback(false, "Erreur lors de la validation")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur validate commande", e)
            withContext(Dispatchers.Main) {
                callback(false, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // ==================== PRESTATIONS ====================

    suspend fun getPrestations(callback: (Boolean, List<PrestationResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.getPrestations()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val prestations = response.body() ?: emptyList()
                        callback(true, prestations, null)
                    } else {
                        callback(false, null, "Erreur lors du chargement des prestations")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get prestations", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun createPrestation(request: PrestationRequest, callback: (Boolean, PrestationResponse?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.createPrestation(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val prestation = response.body()
                        callback(true, prestation, "Prestation créée avec succès")
                    } else {
                        callback(false, null, "Erreur lors de la création de la prestation")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur create prestation", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun cancelPrestation(prestationId: Int, callback: (Boolean, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.cancelPrestation(prestationId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        callback(true, "Prestation annulée avec succès")
                    } else {
                        callback(false, "Erreur lors de l'annulation")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur cancel prestation", e)
            withContext(Dispatchers.Main) {
                callback(false, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // ==================== HELPER METHODS ====================

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

    private fun clearUserInfo() {
        prefs.edit().clear().apply()
    }
}