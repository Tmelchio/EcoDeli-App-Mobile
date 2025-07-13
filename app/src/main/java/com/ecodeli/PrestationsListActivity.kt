package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.PrestationAdapter
import com.ecodeli.models.Prestation
import com.ecodeli.services.ApiService

class PrestationsListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var rvPrestations: RecyclerView
    private lateinit var prestationAdapter: PrestationAdapter

    private val prestationsList = mutableListOf<Prestation>()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestations_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = ApiService()
        userId = prefs.getString("user_id", "") ?: ""

        initViews()
        setupRecyclerView()
        loadPrestations()
    }

    private fun initViews() {
        rvPrestations = findViewById(R.id.rvPrestations)

        // Bouton retour dans la toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mes Prestations"
    }

    private fun setupRecyclerView() {
        prestationAdapter = PrestationAdapter(prestationsList) { prestation ->
            showPrestationDetails(prestation)
        }

        rvPrestations.apply {
            layoutManager = LinearLayoutManager(this@PrestationsListActivity)
            adapter = prestationAdapter
        }
    }

    private fun loadPrestations() {
        apiService.getClientPrestations(userId) { prestations ->
            runOnUiThread {
                prestationsList.clear()
                prestationsList.addAll(prestations)
                prestationAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showPrestationDetails(prestation: Prestation) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la prestation")

        val message = """
            Service: ${prestation.titre}
            Description: ${prestation.description}
            Tarif: ${prestation.tarif}€
            Statut: ${getStatusLabel(prestation.status)}
            Adresse: ${prestation.adresse}
            Durée estimée: ${prestation.dureeEstimee} minutes
        """.trimIndent()

        builder.setMessage(message)

        // Actions selon le statut
        when (prestation.status) {
            "demandee" -> {
                builder.setPositiveButton("Annuler la demande") { _, _ ->
                    cancelPrestation(prestation)
                }
            }
            "terminee" -> {
                builder.setPositiveButton("Laisser un avis") { _, _ ->
                    Toast.makeText(this, "Fonction d'évaluation - À implémenter", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Fermer", null)
        builder.show()
    }

    private fun cancelPrestation(prestation: Prestation) {
        AlertDialog.Builder(this)
            .setTitle("Annuler la prestation")
            .setMessage("Êtes-vous sûr de vouloir annuler cette prestation ?")
            .setPositiveButton("Oui") { _, _ ->
                apiService.cancelPrestation(prestation.id) { success, message ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Prestation annulée", Toast.LENGTH_SHORT).show()
                            loadPrestations() // Recharger la liste
                        } else {
                            Toast.makeText(this, message ?: "Erreur", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "demandee" -> "Demandée"
            "acceptee" -> "Acceptée"
            "en_cours" -> "En cours"
            "terminee" -> "Terminée"
            "annulee" -> "Annulée"
            else -> status
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}