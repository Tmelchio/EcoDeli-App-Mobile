package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.CommandeAdapterReal
import com.ecodeli.models.api.ProductRequestResponse
import com.ecodeli.services.RealApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class CommandesListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvCommandes: RecyclerView
    private lateinit var commandeAdapter: CommandeAdapterReal

    private val commandesList = mutableListOf<ProductRequestResponse>()
    private var userId: String = ""

    companion object {
        private const val TAG = "CommandesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commandes_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)
        userId = prefs.getString("user_id", "") ?: ""

        Log.d(TAG, "Initialisation avec userId: $userId")

        initViews()
        setupRecyclerView()
        loadCommandes()
    }

    private fun initViews() {
        rvCommandes = findViewById(R.id.rvCommandes)

        // Bouton retour dans la toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mes Livraisons"

        Log.d(TAG, "Vues initialisées")
    }

    private fun setupRecyclerView() {
        commandeAdapter = CommandeAdapterReal(commandesList) { productRequest ->
            Log.d(TAG, "Clic sur commande: ${productRequest._id}")
            showCommandeDetails(productRequest)
        }

        rvCommandes.apply {
            layoutManager = LinearLayoutManager(this@CommandesListActivity)
            adapter = commandeAdapter
        }

        Log.d(TAG, "RecyclerView configuré")
    }

    private fun loadCommandes() {
        Log.d(TAG, "Début chargement commandes")

        lifecycleScope.launch {
            apiService.getCommandes { success, productRequests, message ->
                runOnUiThread {
                    if (success && productRequests != null) {
                        Log.d(TAG, "Commandes chargées avec succès: ${productRequests.size}")

                        // Debug des données reçues
                        productRequests.forEachIndexed { index, request ->
                            Log.d(TAG, "Commande $index: ID=${request._id}, product=${request.product?.javaClass}, amount=${request.amount}")
                        }

                        commandesList.clear()
                        commandesList.addAll(productRequests)
                        commandeAdapter.notifyDataSetChanged()

                        Log.d(TAG, "Adapter notifié, ${commandesList.size} éléments")
                    } else {
                        Log.e(TAG, "Erreur chargement commandes: $message")
                        Toast.makeText(this@CommandesListActivity,
                            message ?: "Erreur lors du chargement",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showCommandeDetails(productRequest: ProductRequestResponse) {
        Log.d(TAG, "Affichage détails commande: ${productRequest._id}")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la livraison")

        // Extraire les informations de manière sécurisée
        val productInfo = extractProductInfo(productRequest.product)
        val locationInfo = extractLocationInfo(productRequest.delivery_location)
        val statusInfo = extractStatusInfo(productRequest.delivery_status)
        val sellerInfo = extractSellerInfo(productRequest.product)

        Log.d(TAG, "Infos extraites - Produit: ${productInfo.name}, Prix: ${productInfo.price}, Statut: ${statusInfo.name}")

        val message = """
            Produit: ${productInfo.name}
            Vendeur: ${sellerInfo.name}
            Prix unitaire: ${String.format("%.2f", productInfo.price)}€
            Quantité: ${productRequest.amount}
            Total: ${String.format("%.2f", productInfo.price * productRequest.amount)}€
            Statut: ${statusInfo.displayName}
            Adresse livraison: ${locationInfo.fullAddress}
            Date commande: ${formatDate(productRequest.creation_date)}
        """.trimIndent()

        builder.setMessage(message)

        if (statusInfo.name == "delivered") {
            builder.setPositiveButton("Valider réception") { _, _ ->
                validateCommande(productRequest)
            }
        }

        builder.setNegativeButton("Fermer", null)

        val dialog = builder.create()
        dialog.show()

        Log.d(TAG, "Dialog affiché")
    }

    private fun validateCommande(productRequest: ProductRequestResponse) {
        Log.d(TAG, "Validation commande: ${productRequest._id}")

        AlertDialog.Builder(this)
            .setTitle("Valider la livraison")
            .setMessage("Confirmer la réception de cette livraison ?")
            .setPositiveButton("Valider") { _, _ ->
                lifecycleScope.launch {
                    apiService.validateCommande(productRequest._id) { success, message ->
                        runOnUiThread {
                            if (success) {
                                Log.d(TAG, "Commande validée avec succès")
                                Toast.makeText(this@CommandesListActivity,
                                    "Livraison validée avec succès !",
                                    Toast.LENGTH_SHORT).show()
                                loadCommandes() // Recharger la liste
                            } else {
                                Log.e(TAG, "Erreur validation: $message")
                                Toast.makeText(this@CommandesListActivity,
                                    message ?: "Erreur de validation",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    // ==================== MÉTHODES HELPER ====================

    private data class ProductInfo(
        val name: String,
        val price: Double
    )

    private data class LocationInfo(
        val fullAddress: String
    )

    private data class StatusInfo(
        val name: String,
        val displayName: String
    )

    private data class SellerInfo(
        val name: String
    )

    private fun extractProductInfo(productField: Any?): ProductInfo {
        return try {
            when (productField) {
                is JsonObject -> {
                    val productObj = productField as JsonObject
                    val name = productObj.get("name")?.asString ?: "Produit non spécifié"
                    val price = productObj.get("price")?.asDouble ?: 0.0

                    Log.d(TAG, "ProductInfo extrait (JsonObject): name=$name, price=$price")
                    ProductInfo(name, price)
                }
                is Map<*, *> -> {
                    val productMap = productField as Map<*, *>
                    val name = productMap["name"] as? String ?: "Produit non spécifié"
                    val price = (productMap["price"] as? Number)?.toDouble() ?: 0.0

                    Log.d(TAG, "ProductInfo extrait (Map): name=$name, price=$price")
                    ProductInfo(name, price)
                }
                else -> {
                    Log.w(TAG, "ProductField type inconnu: ${productField?.javaClass}")
                    ProductInfo("Produit non spécifié", 0.0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info produit", e)
            ProductInfo("Produit non spécifié", 0.0)
        }
    }

    private fun extractLocationInfo(locationField: Any?): LocationInfo {
        return try {
            when (locationField) {
                is JsonObject -> {
                    val locationObj = locationField as JsonObject
                    val address = locationObj.get("address")?.asString ?: ""
                    val city = locationObj.get("city")?.asString ?: ""
                    val zipcode = locationObj.get("zipcode")?.asString ?: ""
                    val fullAddress = if (address.isNotEmpty()) {
                        "$address, $zipcode $city"
                    } else {
                        "Adresse non spécifiée"
                    }

                    Log.d(TAG, "LocationInfo extrait (JsonObject): $fullAddress")
                    LocationInfo(fullAddress)
                }
                is Map<*, *> -> {
                    val locationMap = locationField as Map<*, *>
                    val address = locationMap["address"] as? String ?: ""
                    val city = locationMap["city"] as? String ?: ""
                    val zipcode = locationMap["zipcode"] as? String ?: ""
                    val fullAddress = if (address.isNotEmpty()) {
                        "$address, $zipcode $city"
                    } else {
                        "Adresse non spécifiée"
                    }

                    Log.d(TAG, "LocationInfo extrait (Map): $fullAddress")
                    LocationInfo(fullAddress)
                }
                else -> {
                    Log.w(TAG, "LocationField type inconnu: ${locationField?.javaClass}")
                    LocationInfo("Adresse non spécifiée")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info location", e)
            LocationInfo("Adresse non spécifiée")
        }
    }

    private fun extractStatusInfo(statusField: Any?): StatusInfo {
        return try {
            when (statusField) {
                is JsonObject -> {
                    val statusObj = statusField as JsonObject
                    val name = statusObj.get("name")?.asString ?: "pending"
                    val displayName = getStatusLabel(name)

                    Log.d(TAG, "StatusInfo extrait (JsonObject): name=$name, display=$displayName")
                    StatusInfo(name, displayName)
                }
                is Map<*, *> -> {
                    val statusMap = statusField as Map<*, *>
                    val name = statusMap["name"] as? String ?: "pending"
                    val displayName = getStatusLabel(name)

                    Log.d(TAG, "StatusInfo extrait (Map): name=$name, display=$displayName")
                    StatusInfo(name, displayName)
                }
                else -> {
                    Log.w(TAG, "StatusField type inconnu: ${statusField?.javaClass}")
                    StatusInfo("pending", "En attente")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info statut", e)
            StatusInfo("pending", "En attente")
        }
    }

    private fun extractSellerInfo(productField: Any?): SellerInfo {
        return try {
            when (productField) {
                is JsonObject -> {
                    val productObj = productField as JsonObject
                    val sellerObj = productObj.get("seller")?.asJsonObject
                    if (sellerObj != null) {
                        val firstname = sellerObj.get("firstname")?.asString ?: ""
                        val name = sellerObj.get("name")?.asString ?: ""
                        val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                            "$firstname $name"
                        } else {
                            "Vendeur non spécifié"
                        }

                        Log.d(TAG, "SellerInfo extrait (JsonObject): $fullName")
                        SellerInfo(fullName)
                    } else {
                        // Parfois le seller est juste un ID
                        val sellerId = productObj.get("seller")?.asInt
                        if (sellerId != null) {
                            Log.d(TAG, "Seller ID trouvé: $sellerId")
                            // On pourrait faire un appel API pour récupérer les infos, mais pour l'instant on met un placeholder
                            SellerInfo("Vendeur (ID: $sellerId)")
                        } else {
                            SellerInfo("Vendeur non spécifié")
                        }
                    }
                }
                is Map<*, *> -> {
                    val productMap = productField as Map<*, *>
                    val sellerInfo = productMap["seller"]

                    when (sellerInfo) {
                        is Map<*, *> -> {
                            val sellerMap = sellerInfo as Map<*, *>
                            val firstname = sellerMap["firstname"] as? String ?: ""
                            val name = sellerMap["name"] as? String ?: ""
                            val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                                "$firstname $name"
                            } else {
                                "Vendeur non spécifié"
                            }

                            Log.d(TAG, "SellerInfo extrait (Map): $fullName")
                            SellerInfo(fullName)
                        }
                        is Number -> {
                            val sellerId = sellerInfo.toInt()
                            Log.d(TAG, "Seller ID trouvé (Map): $sellerId")
                            SellerInfo("Vendeur (ID: $sellerId)")
                        }
                        else -> {
                            Log.w(TAG, "SellerInfo type inconnu: ${sellerInfo?.javaClass}")
                            SellerInfo("Vendeur non spécifié")
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "ProductField type inconnu pour seller: ${productField?.javaClass}")
                    SellerInfo("Vendeur non spécifié")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info vendeur", e)
            SellerInfo("Vendeur non spécifié")
        }
    }

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "pending" -> "En attente"
            "accepted" -> "Acceptée"
            "in_progress" -> "En cours"
            "delivered" -> "Livrée"
            "cancelled" -> "Annulée"
            else -> status
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Simplifier l'affichage de la date
            if (dateString.contains("T")) {
                dateString.substring(0, 10).replace("-", "/")
            } else {
                dateString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur formatage date: $dateString", e)
            dateString
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "Navigation retour")
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity détruite")
    }
}