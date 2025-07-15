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
import com.ecodeli.models.api.CommandeResponse
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class CommandesListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvCommandes: RecyclerView
    private lateinit var commandeAdapter: CommandeAdapterReal

    private val commandesList = mutableListOf<CommandeResponse>()
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
        commandeAdapter = CommandeAdapterReal(commandesList) { commande ->
            showCommandeDetails(commande)
        }

        rvCommandes.apply {
            layoutManager = LinearLayoutManager(this@CommandesListActivity)
            adapter = commandeAdapter
        }
    }

    private fun loadCommandes() {
        lifecycleScope.launch {
            apiService.getCommandes { success, commandes, message ->
                runOnUiThread {
                    if (success && commandes != null) {
                        commandesList.clear()
                        commandesList.addAll(commandes)
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

    private fun showCommandeDetails(commande: CommandeResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la livraison")

        val message = """
            Commerçant: ${commande.commercant}
            Description: ${commande.description}
            Montant: ${commande.montant}€
            Statut: ${getStatusLabel(commande.status)}
            Adresse: ${commande.adresse_livraison}
            Date: ${formatDate(commande.date_commande)}
        """.trimIndent()

        builder.setMessage(message)

        // Ajouter bouton de validation si livraison terminée
        if (commande.status == "livree") {
            builder.setPositiveButton("Valider réception") { _, _ ->
                validateCommande(commande)
            }
        }

        builder.setNegativeButton("Fermer", null)
        builder.show()
    }

    private fun validateCommande(commande: CommandeResponse) {
        AlertDialog.Builder(this)
            .setTitle("Valider la livraison")
            .setMessage("Confirmer la réception de cette livraison ?")
            .setPositiveButton("Valider") { _, _ ->
                lifecycleScope.launch {
                    apiService.validateCommande(commande._id) { success, message ->
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
            "en_attente" -> "En attente"
            "en_livraison" -> "En livraison"
            "livree" -> "Livrée"
            "validee" -> "Validée"
            "annulee" -> "Annulée"
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