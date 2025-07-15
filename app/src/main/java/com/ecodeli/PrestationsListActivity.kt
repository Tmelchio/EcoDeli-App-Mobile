package com.ecodeli

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.PrestationAdapter
import com.ecodeli.models.Prestation
import com.ecodeli.services.RealApiService

class PrestationsListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvPrestations: RecyclerView
    private lateinit var prestationAdapter: PrestationAdapter

    private val prestationsList = mutableListOf<Prestation>()
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
        prestationAdapter = PrestationAdapter(prestationsList) { prestation ->
            showPrestationDetails(prestation)
        }

        rvPrestations.apply {
            layoutManager = LinearLayoutManager(this@PrestationsListActivity)
            adapter = prestationAdapter
        }
    }

    private fun loadPrestations() {
        // TODO: Implémenter le chargement des prestations depuis l'API réelle
        // Pour l'instant, afficher un message
        Toast.makeText(this, "Chargement des prestations - En cours de développement", Toast.LENGTH_SHORT).show()

        // Vider la liste pour l'instant
        prestationsList.clear()
        prestationAdapter.notifyDataSetChanged()
    }

    private fun showPrestationDetails(prestation: Prestation) {
        // TODO: Implémenter l'affichage des détails avec l'API réelle
        Toast.makeText(this, "Détails de prestation - En cours de développement", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}