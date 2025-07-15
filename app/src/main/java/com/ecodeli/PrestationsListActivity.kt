package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.PrestationAdapterReal
import com.ecodeli.models.api.PrestationResponse
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class PrestationsListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvPrestations: RecyclerView
    private lateinit var prestationAdapter: PrestationAdapterReal

    private val prestationsList = mutableListOf<PrestationResponse>()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestations_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)
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
        prestationAdapter = PrestationAdapterReal(prestationsList) { prestation ->
            showPrestationDetails(prestation)
        }

        rvPrestations.apply {
            layoutManager = LinearLayoutManager(this@PrestationsListActivity)
            adapter = prestationAdapter
        }
    }

    private fun loadPrestations() {
        lifecycleScope.launch {
            apiService.getPrestations { success, prestations, message ->
                runOnUiThread {
                    if (success && prestations != null) {
                        prestationsList.clear()
                        prestationsList.addAll(prestations)
                        prestationAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@PrestationsListActivity,
                            message ?: "Erreur lors du chargement",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showPrestationDetails(prestation: PrestationResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la prestation")

        val message = """
            Service: ${prestation.titre}
            Description: ${prestation.description}
            Tarif: ${prestation.tarif}€
            Statut: ${getStatusLabel(prestation.status)}
            Adresse: ${prestation.adresse}
            Durée estimée: ${prestation.duree_estimee} minutes
            Date: ${formatDate(prestation.date_prestation)}
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

    private fun cancelPrestation(prestation: PrestationResponse) {
        AlertDialog.Builder(this)
            .setTitle("Annuler la prestation")
            .setMessage("Êtes-vous sûr de vouloir annuler cette prestation ?")
            .setPositiveButton("Oui") { _, _ ->
                lifecycleScope.launch {
                    apiService.cancelPrestation(prestation._id) { success, message ->
                        runOnUiThread {
                            if (success) {
                                Toast.makeText(this@PrestationsListActivity,
                                    "Prestation annulée",
                                    Toast.LENGTH_SHORT).show()
                                loadPrestations() // Recharger la liste
                            } else {
                                Toast.makeText(this@PrestationsListActivity,
                                    message ?: "Erreur",
                                    Toast.LENGTH_SHORT).show()
                            }
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