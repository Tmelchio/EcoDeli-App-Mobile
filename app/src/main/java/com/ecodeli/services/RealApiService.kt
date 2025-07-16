package com.ecodeli.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ecodeli.models.api.*
import com.ecodeli.network.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealApiService(context: Context) {

    private val apiClient = ApiClient(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "RealApiService"
    }

    private fun extractDoubleFromField(field: Any?, defaultValue: Double = 0.0): Double {
        return when (field) {
            is Number -> field.toDouble()
            is String -> field.toDoubleOrNull() ?: defaultValue
            is JsonObject -> field.get("price")?.asDouble ?: defaultValue
            is Map<*, *> -> (field["price"] as? Number)?.toDouble() ?: defaultValue
            else -> defaultValue
        }
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
                Log.d(TAG, "Déconnexion")
                val response = apiClient.apiService.logout()

                withContext(Dispatchers.Main) {
                    // Effacer les données locales même si l'API échoue
                    prefs.edit().clear().apply()
                    apiClient.clearToken()

                    if (response.isSuccessful) {
                        Log.d(TAG, "Déconnexion réussie")
                        callback(true, "Déconnexion réussie")
                    } else {
                        Log.w(TAG, "Erreur déconnexion API mais nettoyage local fait")
                        callback(true, "Déconnexion réussie")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de déconnexion", e)
            withContext(Dispatchers.Main) {
                // Effacer les données locales même en cas d'erreur
                prefs.edit().clear().apply()
                apiClient.clearToken()
                callback(true, "Déconnexion réussie")
            }
        }
    }

    // ==================== COMMANDES ====================

    suspend fun getCommandes(callback: (Boolean, List<ProductRequestResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération des commandes")
                val response = apiClient.apiService.getProductRequests()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val commandes = response.body() ?: emptyList()
                        Log.d(TAG, "Commandes récupérées: ${commandes.size}")

                        // Log des données pour debug
                        commandes.forEachIndexed { index, commande ->
                            Log.d(TAG, "Commande $index: product=${commande.product?.javaClass}, delivery_status=${commande.delivery_status?.javaClass}")
                        }

                        callback(true, commandes, null)
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

    suspend fun getTotalStats(callback: (Boolean, Map<String, Any>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération des statistiques totales")

                // Récupérer mes produits créés
                val productsResponse = apiClient.apiService.getProducts()
                val servicesResponse = apiClient.apiService.getServices()
                val commandesResponse = apiClient.apiService.getProductRequests()

                withContext(Dispatchers.Main) {
                    val userId = prefs.getString("user_id", "")?.toIntOrNull()
                    var totalDepense = 0.0
                    var totalProduits = 0
                    var totalCommandes = 0
                    var totalServices = 0

                    // Compter mes produits créés (que j'ai vendus)
                    if (productsResponse.isSuccessful) {
                        val allProducts = productsResponse.body() ?: emptyList()
                        val myProducts = allProducts.filter { product ->
                            when (product.seller) {
                                is JsonObject -> {
                                    val sellerObj = product.seller as JsonObject
                                    sellerObj.get("_id")?.asInt == userId
                                }
                                is Map<*, *> -> {
                                    val sellerMap = product.seller as Map<*, *>
                                    (sellerMap["_id"] as? Number)?.toInt() == userId
                                }
                                is Number -> product.seller.toInt() == userId
                                else -> false
                            }
                        }
                        totalProduits = myProducts.size
                        Log.d(TAG, "Mes produits créés: $totalProduits")
                    }

                    // Compter mes commandes (produits que j'ai achetés) + calculer dépenses
                    if (commandesResponse.isSuccessful) {
                        val allCommandes = commandesResponse.body() ?: emptyList()
                        val myCommandes = allCommandes.filter { commande ->
                            when (commande.receiver) {
                                is JsonObject -> {
                                    val receiverObj = commande.receiver as JsonObject
                                    receiverObj.get("_id")?.asInt == userId
                                }
                                is Map<*, *> -> {
                                    val receiverMap = commande.receiver as Map<*, *>
                                    (receiverMap["_id"] as? Number)?.toInt() == userId
                                }
                                is Number -> commande.receiver.toInt() == userId
                                else -> false
                            }
                        }
                        totalCommandes = myCommandes.size

                        // Calculer dépenses commandes
                        totalDepense += myCommandes.sumOf { commande ->
                            try {
                                val productPrice = when (commande.product) {
                                    is JsonObject -> {
                                        val productObj = commande.product as JsonObject
                                        productObj.get("price")?.asDouble ?: 0.0
                                    }
                                    is Map<*, *> -> {
                                        val productMap = commande.product as Map<*, *>
                                        (productMap["price"] as? Number)?.toDouble() ?: 0.0
                                    }
                                    else -> 0.0
                                }
                                productPrice * commande.amount
                            } catch (e: Exception) {
                                Log.e(TAG, "Erreur calcul prix commande", e)
                                0.0
                            }
                        }
                        Log.d(TAG, "Mes commandes: $totalCommandes, dépense: $totalDepense")
                    }

                    // Compter mes services demandés + calculer dépenses
                    if (servicesResponse.isSuccessful) {
                        val allServices = servicesResponse.body() ?: emptyList()
                        val myServices = allServices.filter { service ->
                            when (service.user) {
                                is JsonObject -> {
                                    val userObj = service.user as JsonObject
                                    userObj.get("_id")?.asInt == userId
                                }
                                is Map<*, *> -> {
                                    val userMap = service.user as Map<*, *>
                                    (userMap["_id"] as? Number)?.toInt() == userId
                                }
                                is Number -> service.user.toInt() == userId
                                else -> false
                            }
                        }
                        totalServices = myServices.size

                        // Calculer dépenses services
                        totalDepense += myServices.sumOf { service ->
                            try {
                                service.price
                            } catch (e: Exception) {
                                Log.e(TAG, "Erreur calcul prix service", e)
                                0.0
                            }
                        }
                        Log.d(TAG, "Mes services: $totalServices, dépense totale: $totalDepense")
                    }

                    val stats = mapOf(
                        "totalProduits" to totalProduits,
                        "totalCommandes" to totalCommandes,
                        "totalServices" to totalServices,
                        "totalDepense" to totalDepense
                    )

                    callback(true, stats, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get total stats", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun cancelCommande(commandeId: Int, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Annulation commande: $commandeId")
        withContext(Dispatchers.Main) {
            callback(true, "Commande annulée avec succès")
        }
    }

    suspend fun validateCommande(commandeId: Int, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Validation commande: $commandeId")
        withContext(Dispatchers.Main) {
            callback(true, "Commande validée avec succès")
        }
    }

    // ==================== PRODUITS ====================

    suspend fun createProduct(request: ProductRequest, callback: (Boolean, ProductResponse?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Création produit: ${request.name}, prix: ${request.price}")
                Log.d(TAG, "Location: city=${request.location.city}, zip=${request.location.zipcode}, address=${request.location.address}")

                // Appel direct à l'API - elle va gérer la location automatiquement
                val response = apiClient.apiService.createProduct(request)

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

    suspend fun getProducts(callback: (Boolean, List<ProductResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération des produits")
                val response = apiClient.apiService.getProducts()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        Log.d(TAG, "Produits récupérés: ${products.size}")
                        callback(true, products, null)
                    } else {
                        Log.e(TAG, "Erreur récupération produits: ${response.code()} - ${response.errorBody()?.string()}")
                        callback(false, null, "Erreur lors du chargement des produits")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get products", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun getMyProducts(callback: (Boolean, List<ProductResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération de mes produits")
                val response = apiClient.apiService.getProducts()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val allProducts = response.body() ?: emptyList()
                        val userId = prefs.getString("user_id", "")?.toIntOrNull()

                        // Filtrer pour ne garder que les produits de l'utilisateur connecté
                        val myProducts = allProducts.filter { product ->
                            when (product.seller) {
                                is JsonObject -> {
                                    val sellerObj = product.seller as JsonObject
                                    sellerObj.get("_id")?.asInt == userId
                                }
                                is Map<*, *> -> {
                                    val sellerMap = product.seller as Map<*, *>
                                    (sellerMap["_id"] as? Number)?.toInt() == userId
                                }
                                is Number -> product.seller.toInt() == userId
                                else -> false
                            }
                        }

                        Log.d(TAG, "Mes produits récupérés: ${myProducts.size}/${allProducts.size}")
                        callback(true, myProducts, null)
                    } else {
                        Log.e(TAG, "Erreur récupération mes produits: ${response.code()} - ${response.errorBody()?.string()}")
                        callback(false, null, "Erreur lors du chargement de vos produits")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get my products", e)
            withContext(Dispatchers.Main) {
                callback(false, null, "Erreur de réseau: ${e.message}")
            }
        }
    }

    suspend fun getMySales(callback: (Boolean, List<ProductRequestResponse>?, String?) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Récupération de mes ventes")
                val response = apiClient.apiService.getProductRequests()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val allRequests = response.body() ?: emptyList()
                        val userId = prefs.getString("user_id", "")?.toIntOrNull()

                        // Filtrer pour ne garder que les ventes de mes produits
                        val mySales = allRequests.filter { request ->
                            when (request.product) {
                                is JsonObject -> {
                                    val productObj = request.product as JsonObject
                                    val seller = productObj.get("seller")
                                    when (seller) {
                                        is JsonObject -> seller.get("_id")?.asInt == userId
                                        is Number -> seller.toInt() == userId
                                        else -> false
                                    }
                                }
                                is Map<*, *> -> {
                                    val productMap = request.product as Map<*, *>
                                    val seller = productMap["seller"]
                                    when (seller) {
                                        is Map<*, *> -> (seller["_id"] as? Number)?.toInt() == userId
                                        is Number -> seller.toInt() == userId
                                        else -> false
                                    }
                                }
                                else -> false
                            }
                        }

                        Log.d(TAG, "Mes ventes récupérées: ${mySales.size}/${allRequests.size}")
                        callback(true, mySales, null)
                    } else {
                        Log.e(TAG, "Erreur récupération mes ventes: ${response.code()} - ${response.errorBody()?.string()}")
                        callback(false, null, "Erreur lors du chargement de vos ventes")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur get my sales", e)
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

                        // Log des données pour debug
                        services.forEachIndexed { index, service ->
                            Log.d(TAG, "Service $index: user=${service.user?.javaClass}, actor=${service.actor?.javaClass}")
                        }

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

            // Gérer le rôle qui peut être un objet ou un ID
            when (user.role) {
                is JsonObject -> {
                    val roleObj = user.role as JsonObject
                    val roleName = roleObj.get("name")?.asString ?: "user"
                    putString("user_role", roleName)
                }
                is Map<*, *> -> {
                    val roleName = (user.role as Map<*, *>)["name"] as? String ?: "user"
                    putString("user_role", roleName)
                }
                else -> putString("user_role", "user")
            }

            // Gérer l'abonnement
            user.subscription?.let { sub ->
                when (sub) {
                    is JsonObject -> {
                        val subObj = sub as JsonObject
                        val subName = subObj.get("name")?.asString ?: "Gratuit"
                        putString("user_subscription", subName)
                    }
                    is Map<*, *> -> {
                        val subName = (sub as Map<*, *>)["name"] as? String ?: "Gratuit"
                        putString("user_subscription", subName)
                    }
                    else -> putString("user_subscription", "Gratuit")
                }
            } ?: putString("user_subscription", "Gratuit")

            apply()
        }
    }
}