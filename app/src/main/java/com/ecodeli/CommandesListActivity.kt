package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.CommandeAdapterReal
import com.ecodeli.models.api.ProductRequestResponse
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class CommandesListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvCommandes: RecyclerView
    private lateinit var commandeAdapter: CommandeAdapterReal

    private val commandesList = mutableListOf<ProductRequestResponse>()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commandes_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)
        userId = prefs.getString("user_id", "") ?: ""

        initViews()
        setupRecyclerView()
        loadCommandes()
    }

    private fun initViews() {
        rvCommandes = findViewById(R.id.rvCommandes)

        // Bouton retour dans la toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mes Livraisons"
    }

    private fun setupRecyclerView() {
        commandeAdapter = CommandeAdapterReal(commandesList) { productRequest ->
            showCommandeDetails(productRequest)
        }

        rvCommandes.apply {
            layoutManager = LinearLayoutManager(this@CommandesListActivity)
            adapter = commandeAdapter
        }
    }

    private fun loadCommandes() {
        lifecycleScope.launch {
            apiService.getCommandes { success, productRequests, message ->
                runOnUiThread {
                    if (success && productRequests != null) {
                        commandesList.clear()
                        commandesList.addAll(productRequests)
                        commandeAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@CommandesListActivity,
                            message ?: "Erreur lors du chargement",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showCommandeDetails(productRequest: ProductRequestResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la livraison")

        val product = productRequest.product
        val sellerId = product?.seller as? Int
        val deliveryLocation = productRequest.delivery_location
        val deliveryStatus = productRequest.delivery_status

        if (sellerId != null) {
            lifecycleScope.launch {
                val userInfo = apiService.getSellerById(sellerId)
                val sellerName = if (userInfo != null) {
                    "${userInfo.firstname} ${userInfo.name}"
                } else {
                    "Non spécifié"
                }

                val message = """
                Produit: ${product?.name ?: "Non spécifié"}
                Vendeur: $sellerName
                Prix unitaire: ${product?.price ?: 0.0}€
                Quantité: ${productRequest.amount}
                Total: ${(product?.price ?: 0.0) * productRequest.amount}€
                Statut: ${getStatusLabel(deliveryStatus?.name ?: "pending")}
                Adresse livraison: ${deliveryLocation?.address ?: "Non spécifiée"}
                Date commande: ${formatDate(productRequest.creation_date)}
            """.trimIndent()

                builder.setMessage(message)
                if (deliveryStatus?.name == "delivered") {
                    builder.setPositiveButton("Valider réception") { _, _ ->
                        validateCommande(productRequest)
                    }
                }
                builder.setNegativeButton("Fermer", null)
                builder.show()
            }
        } else {
            val message = """
            Produit: ${product?.name ?: "Non spécifié"}
            Vendeur: Non spécifié
            Prix unitaire: ${product?.price ?: 0.0}€
            Quantité: ${productRequest.amount}
            Total: ${(product?.price ?: 0.0) * productRequest.amount}€
            Statut: ${getStatusLabel(deliveryStatus?.name ?: "pending")}
            Adresse livraison: ${deliveryLocation?.address ?: "Non spécifiée"}
            Date commande: ${formatDate(productRequest.creation_date)}
        """.trimIndent()

            builder.setMessage(message)
            if (deliveryStatus?.name == "delivered") {
                builder.setPositiveButton("Valider réception") { _, _ ->
                    validateCommande(productRequest)
                }
            }
            builder.setNegativeButton("Fermer", null)
            builder.show()
        }
    }


    private fun validateCommande(productRequest: ProductRequestResponse) {
        AlertDialog.Builder(this)
            .setTitle("Valider la livraison")
            .setMessage("Confirmer la réception de cette livraison ?")
            .setPositiveButton("Valider") { _, _ ->
                lifecycleScope.launch {
                    apiService.validateCommande(productRequest._id) { success, message ->
                        runOnUiThread {
                            if (success) {
                                Toast.makeText(this@CommandesListActivity,
                                    "Livraison validée avec succès !",
                                    Toast.LENGTH_SHORT).show()
                                loadCommandes() // Recharger la liste
                            } else {
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
            dateString.substring(0, 10).replace("-", "/")
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}