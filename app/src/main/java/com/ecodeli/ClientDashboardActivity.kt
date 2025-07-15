package com.ecodeli

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.ecodeli.adapters.CommandeAdapter
import com.ecodeli.models.Commande
import com.ecodeli.models.Prestation
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class ClientDashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService

    // Views principales
    private lateinit var tvWelcome: TextView
    private lateinit var tvTotalCommandes: TextView
    private lateinit var tvTotalPrestations: TextView
    private lateinit var tvTotalDepense: TextView
    private lateinit var btnNouvelleCommande: Button
    private lateinit var btnDemanderPrestation: Button
    private lateinit var btnMesCommandes: Button
    private lateinit var btnMesPrestations: Button

    // RecyclerView pour activités récentes
    private lateinit var rvActivitesRecentes: RecyclerView
    private lateinit var activiteAdapter: CommandeAdapter

    private val commandesList = mutableListOf<Commande>()

    // Données utilisateur
    private var userId: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        // Initialiser les services
        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)

        // Récupérer les données utilisateur
        userId = prefs.getString("user_id", "") ?: ""
        userEmail = prefs.getString("user_email", "") ?: ""

        // Initialiser les vues
        initViews()
        setupClickListeners()
        setupRecyclerView()

        // Charger les données depuis l'API réelle
        loadUserData()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvTotalCommandes = findViewById(R.id.tvTotalCommandes)
        tvTotalPrestations = findViewById(R.id.tvTotalPrestations)
        tvTotalDepense = findViewById(R.id.tvTotalDepense)
        btnNouvelleCommande = findViewById(R.id.btnNouvelleCommande)
        btnDemanderPrestation = findViewById(R.id.btnDemanderPrestation)
        btnMesCommandes = findViewById(R.id.btnMesCommandes)
        btnMesPrestations = findViewById(R.id.btnMesPrestations)
        rvActivitesRecentes = findViewById(R.id.rvActivitesRecentes)

        // Personnaliser le message de bienvenue
        val firstname = prefs.getString("user_firstname", "") ?: ""
        val name = prefs.getString("user_name", "") ?: ""

        val displayName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
            "$firstname $name"
        } else {
            userEmail
        }

        tvWelcome.text = "Bienvenue, $displayName"
    }

    private fun setupClickListeners() {
        btnNouvelleCommande.setOnClickListener {
            showCreateCommandeDialog()
        }

        btnDemanderPrestation.setOnClickListener {
            showPrestationTypesDialog()
        }

        btnMesCommandes.setOnClickListener {
            val intent = Intent(this, CommandesListActivity::class.java)
            startActivity(intent)
        }

        btnMesPrestations.setOnClickListener {
            val intent = Intent(this, PrestationsListActivity::class.java)
            startActivity(intent)
        }

        val btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        activiteAdapter = CommandeAdapter(commandesList) { commande ->
            showCommandeDetails(commande)
        }

        rvActivitesRecentes.apply {
            layoutManager = LinearLayoutManager(this@ClientDashboardActivity)
            adapter = activiteAdapter
        }
    }

    private fun loadUserData() {
        // Charger les statistiques utilisateur depuis l'API réelle
        // TODO: Implémenter les appels API réels pour les statistiques
        // Pour l'instant, afficher des valeurs par défaut
        tvTotalCommandes.text = "0"
        tvTotalPrestations.text = "0"
        tvTotalDepense.text = "0€"

        // TODO: Charger les commandes récentes depuis l'API
        loadRecentActivities()
    }

    private fun loadRecentActivities() {
        // TODO: Implémenter le chargement des activités récentes depuis l'API réelle
        // Pour l'instant, vider la liste
        commandesList.clear()
        activiteAdapter.notifyDataSetChanged()
    }

    private fun showCreateCommandeDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_commande, null)
        builder.setView(view)

        val etCommercant = view.findViewById<EditText>(R.id.etCommercant)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etMontant = view.findViewById<EditText>(R.id.etMontant)
        val etAdresse = view.findViewById<EditText>(R.id.etAdresse)
        val btnCreer = view.findViewById<Button>(R.id.btnCreer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)

        val dialog = builder.create()

        btnCreer.setOnClickListener {
            val commercant = etCommercant.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val montantStr = etMontant.text.toString().trim()
            val adresse = etAdresse.text.toString().trim()

            if (commercant.isEmpty() || description.isEmpty() || montantStr.isEmpty() || adresse.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val montant = try {
                montantStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            // TODO: Implémenter la création de commande avec l'API réelle
            lifecycleScope.launch {
                // Simuler un délai pour l'instant
                kotlinx.coroutines.delay(1000)

                runOnUiThread {
                    btnCreer.isEnabled = true
                    btnCreer.text = "Créer"
                    Toast.makeText(this@ClientDashboardActivity, "Fonctionnalité en cours de développement", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnAnnuler.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPrestationTypesDialog() {
        val prestationTypes = arrayOf(
            "Transport de personne (médecin, gare...)",
            "Transfert aéroport",
            "Courses personnalisées",
            "Achat produit à l'étranger",
            "Garde d'animaux à domicile",
            "Petits travaux ménagers",
            "Jardinage"
        )

        AlertDialog.Builder(this)
            .setTitle("Quel service demandez-vous ?")
            .setItems(prestationTypes) { _, which ->
                showCreatePrestationDialog(prestationTypes[which])
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showCreatePrestationDialog(typePrestation: String) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_prestation, null)
        builder.setView(view)

        val tvType = view.findViewById<TextView>(R.id.tvTypePrestation)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etAdresse = view.findViewById<EditText>(R.id.etAdresse)
        val etDateSouhaitee = view.findViewById<EditText>(R.id.etDateSouhaitee)
        val etDureeEstimee = view.findViewById<EditText>(R.id.etDureeEstimee)
        val etBudget = view.findViewById<EditText>(R.id.etBudget)
        val btnCreer = view.findViewById<Button>(R.id.btnCreer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)

        tvType.text = typePrestation

        val dialog = builder.create()

        btnCreer.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val adresse = etAdresse.text.toString().trim()
            val dateSouhaitee = etDateSouhaitee.text.toString().trim()

            if (description.isEmpty() || adresse.isEmpty() || dateSouhaitee.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            // TODO: Implémenter la création de prestation avec l'API réelle
            lifecycleScope.launch {
                // Simuler un délai pour l'instant
                kotlinx.coroutines.delay(1000)

                runOnUiThread {
                    btnCreer.isEnabled = true
                    btnCreer.text = "Créer"
                    Toast.makeText(this@ClientDashboardActivity, "Fonctionnalité en cours de développement", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnAnnuler.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCommandeDetails(commande: Commande) {
        // TODO: Implémenter l'affichage des détails avec l'API réelle
        Toast.makeText(this, "Détails de commande - En cours de développement", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}