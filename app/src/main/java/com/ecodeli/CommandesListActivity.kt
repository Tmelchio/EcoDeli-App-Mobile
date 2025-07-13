package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.CommandeAdapter
import com.ecodeli.models.Commande
import com.ecodeli.services.ApiService

class CommandesListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var rvCommandes: RecyclerView
    private lateinit var commandeAdapter: CommandeAdapter

    private val commandesList = mutableListOf<Commande>()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commandes_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = ApiService()
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
        commandeAdapter = CommandeAdapter(commandesList) { commande ->
            showCommandeDetails(commande)
        }

        rvCommandes.apply {
            layoutManager = LinearLayoutManager(this@CommandesListActivity)
            adapter = commandeAdapter
        }
    }

    private fun loadCommandes() {
        apiService.getClientCommandes(userId) { commandes ->
            runOnUiThread {
                commandesList.clear()
                commandesList.addAll(commandes)
                commandeAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showCommandeDetails(commande: Commande) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la livraison")

        val message = """
            Commerçant: ${commande.commercant}
            Description: ${commande.description}
            Montant: ${commande.montant}€
            Statut: ${getStatusLabel(commande.status)}
            Adresse: ${commande.adresseLivraison}
        """.trimIndent()

        builder.setMessage(message)

        // Ajouter bouton de validation si livraison terminée
        if (commande.status == "livree") {
            builder.setPositiveButton("Valider réception") { _, _ ->
                showValidationDialog(commande)
            }
        }

        builder.setNegativeButton("Fermer", null)
        builder.show()
    }

    private fun showValidationDialog(commande: Commande) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Valider la livraison")
        builder.setMessage("Saisissez le code de validation fourni par le livreur :")

        val input = EditText(this)
        input.hint = "Code de validation"
        builder.setView(input)

        builder.setPositiveButton("Valider") { _, _ ->
            val code = input.text.toString().trim()
            if (code.isNotEmpty()) {
                validateLivraison(commande, code)
            } else {
                Toast.makeText(this, "Code requis", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }

    private fun validateLivraison(commande: Commande, code: String) {
        apiService.validateLivraison(commande.id, code) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Livraison validée avec succès !", Toast.LENGTH_SHORT).show()
                    loadCommandes() // Recharger la liste
                } else {
                    Toast.makeText(this, message ?: "Erreur de validation", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}