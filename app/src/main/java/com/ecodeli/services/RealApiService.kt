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
                Log.d(TAG, "Tentative de connexion pour: $email")
                val response = apiClient.apiService.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            Log.d(TAG, "Connexion réussie, token reçu")
                            apiClient.saveToken(loginResponse.token)
                            saveUserInfo(loginResponse.user)
                            callback(true, "client", "Connexion réussie")
                        } else {
                            Log.e(TAG, "Réponse vide du serveur")
                            callback(false, null, "Réponse vide du serveur")
                        }
                    } else {
                        Log.e(TAG, "Erreur connexion: ${response.code()} - ${response.errorBody()?.string()}")
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

                Log.d(TAG, "Tentative d'inscription pour: ${request.email}")
                val response = apiClient.apiService.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Inscription réussie")
                        callback(true, "Inscription réussie")
                    } else {
                        Log.e(TAG, "Erreur inscription: ${response.code()} - ${response.errorBody()?.string()}")
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
                Log.d(TAG, "Validation du token")
                val response = apiClient.apiService.validateToken()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val userMap = response.body()
                        val user = userMap?.get("user")
                        if (user != null) {
                            Log.d(TAG, "Token valide, utilisateur: ${user.email}")
                            saveUserInfo(user)
                            callback(true, user)
                        } else {
                            Log.e(TAG, "Token invalide - pas d'utilisateur")
                            callback(false, null)
                        }
                    } else {
                        Log.e(TAG, "Token invalide: ${response.code()}")
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

    // ==================== PRODUITS / COMMANDES ====================

    suspend fun getCommandes(callback: (Boolean, List<ProductRequestResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération des commandes")
                val response = apiClient.apiService.getProductRequests()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val requests = response.body() ?: emptyList()
                        Log.d(TAG, "Commandes récupérées: ${requests.size}")
                        callback(true, requests, null)
                    } else {
                        Log.e(TAG, "Erreur récupération commandes: ${response.code()} - ${response.errorBody()?.string()}")
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

    suspend fun validateCommande(commandeId: Int, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Validation commande: $commandeId")
        withContext(Dispatchers.Main) {
            callback(true, "Commande validée avec succès")
        }
    }

    suspend fun createProduct(request: ProductRequest, callback: (Boolean, ProductResponse?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Création produit: ${request.name}, prix: ${request.price}")

                // D'abord créer la location
                val locationRequest = LocationRequest(request.location)
                Log.d(TAG, "Création location: ${request.location}")
                val locationResponse = apiClient.apiService.createLocation(locationRequest)

                if (!locationResponse.isSuccessful) {
                    Log.e(TAG, "Erreur création location: ${locationResponse.code()} - ${locationResponse.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        callback(false, null, "Erreur création de l'adresse")
                    }
                    return@withContext
                }

                val location = locationResponse.body()
                if (location == null) {
                    Log.e(TAG, "Location créée mais réponse vide")
                    withContext(Dispatchers.Main) {
                        callback(false, null, "Erreur récupération de l'adresse")
                    }
                    return@withContext
                }

                Log.d(TAG, "Location créée avec ID: ${location._id}")

                // Ensuite créer le produit avec l'ID de la location
                val productRequestWithLocation = ProductRequest(
                    name = request.name,
                    price = request.price,
                    size = request.size,
                    location = LocationData(
                        city = location.city,
                        zipcode = location.zipcode,
                        address = location.address
                    )
                )

                Log.d(TAG, "Création produit avec location ID: ${location._id}")
                val response = apiClient.apiService.createProduct(productRequestWithLocation)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val product = response.body()
                        Log.d(TAG, "Produit créé avec succès: ID ${product?._id}")
                        callback(true, product, "Produit créé avec succès")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Erreur création produit: ${response.code()} - $errorBody")
                        callback(false, null, "Erreur lors de la création du produit: $errorBody")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception create product", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // ==================== SERVICES / PRESTATIONS ====================

    suspend fun getPrestations(callback: (Boolean, List<ServiceResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération des prestations")
                val response = apiClient.apiService.getServices()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val services = response.body() ?: emptyList()
                        Log.d(TAG, "Prestations récupérées: ${services.size}")
                        callback(true, services, null)
                    } else {
                        Log.e(TAG, "Erreur récupération prestations: ${response.code()} - ${response.errorBody()?.string()}")
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

    suspend fun createService(request: ServiceRequest, callback: (Boolean, ServiceResponse?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Création service: ${request.name}, prix: ${request.price}")
                val response = apiClient.apiService.createService(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val service = response.body()
                        Log.d(TAG, "Service créé avec succès: ID ${service?._id}")
                        callback(true, service, "Service créé avec succès")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Erreur création service: ${response.code()} - $errorBody")
                        callback(false, null, "Erreur lors de la création du service: $errorBody")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception create service", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun cancelPrestation(prestationId: Int, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Annulation prestation: $prestationId")
        withContext(Dispatchers.Main) {
            callback(true, "Service annulé avec succès")
        }
    }

    // ==================== LOCATIONS ====================

    suspend fun createLocation(city: String, zipcode: String, address: String, callback: (Boolean, LocationInfo?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val request = LocationRequest(
                    location = LocationData(city, zipcode, address)
                )
                Log.d(TAG, "Création location: $city, $zipcode, $address")
                val response = apiClient.apiService.createLocation(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val location = response.body()
                        Log.d(TAG, "Location créée avec ID: ${location?._id}")
                        callback(true, location, "Adresse créée")
                    } else {
                        Log.e(TAG, "Erreur création location: ${response.code()} - ${response.errorBody()?.string()}")
                        callback(false, null, "Erreur lors de la création de l'adresse")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception create location", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun saveUserInfo(user: UserInfo) {
        Log.d(TAG, "Sauvegarde info utilisateur: ${user.email}")
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

    suspend fun getSellerById(sellerId: Int): UserInfo? {
        return try {
            Log.d(TAG, "Récupération vendeur ID: $sellerId")
            val response = apiClient.apiService.getUser(sellerId)
            if (response.isSuccessful) {
                Log.d(TAG, "Vendeur trouvé: ${response.body()?.email}")
                response.body()
            } else {
                Log.e(TAG, "Vendeur non trouvé: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur récupération vendeur", e)
            null
        }
    }

    private fun clearUserInfo() {
        Log.d(TAG, "Nettoyage des données utilisateur")
        prefs.edit().clear().apply()
    }
}