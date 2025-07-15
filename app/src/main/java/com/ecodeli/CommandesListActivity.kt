package com.ecodeli

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.CommandeAdapter
import com.ecodeli.models.Commande
import com.ecodeli.services.RealApiService

class CommandesListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvCommandes: RecyclerView
    private lateinit var commandeAdapter: CommandeAdapter

    private val commandesList = mutableListOf<Commande>()
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
        commandeAdapter = CommandeAdapter(commandesList) { commande ->
            showCommandeDetails(commande)
        }

        rvCommandes.apply {
            layoutManager = LinearLayoutManager(this@CommandesListActivity)
            adapter = commandeAdapter
        }
    }

    private fun loadCommandes() {
        // TODO: Implémenter le chargement des commandes depuis l'API réelle
        // Pour l'instant, afficher un message
        Toast.makeText(this, "Chargement des commandes - En cours de développement", Toast.LENGTH_SHORT).show()

        // Vider la liste pour l'instant
        commandesList.clear()
        commandeAdapter.notifyDataSetChanged()
    }

    private fun showCommandeDetails(commande: Commande) {
        // TODO: Implémenter l'affichage des détails avec l'API réelle
        Toast.makeText(this, "Détails de commande - En cours de développement", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}