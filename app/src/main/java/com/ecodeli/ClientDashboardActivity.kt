package com.ecodeli

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.models.api.CommandeRequest
import com.ecodeli.models.api.PrestationRequest
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    private fun loadUserData() {
        // Charger les statistiques depuis l'API
        loadStatistiques()
    }

    private fun loadStatistiques() {
        lifecycleScope.launch {
            // Charger les commandes pour calculer les stats
            apiService.getCommandes { success, commandes, _ ->
                if (success && commandes != null) {
                    tvTotalCommandes.text = commandes.size.toString()
                    val totalCommandesDepense = commandes.sumOf { it.montant }

                    // Charger les prestations
                    lifecycleScope.launch {
                        apiService.getPrestations { successP, prestations, _ ->
                            val totalPrestations = if (successP && prestations != null) {
                                prestations.size
                            } else 0

                            val totalPrestationsDepense = if (successP && prestations != null) {
                                prestations.sumOf { it.tarif }
                            } else 0.0

                            tvTotalPrestations.text = totalPrestations.toString()
                            val totalDepense = totalCommandesDepense + totalPrestationsDepense
                            tvTotalDepense.text = String.format("%.2f€", totalDepense)
                        }
                    }
                } else {
                    // Valeurs par défaut en cas d'erreur
                    tvTotalCommandes.text = "0"
                    tvTotalPrestations.text = "0"
                    tvTotalDepense.text = "0€"
                }
            }
        }
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

            // Utiliser la vraie API
            val request = CommandeRequest(
                commercant = commercant,
                description = description,
                montant = montant,
                adresse_livraison = adresse
            )

            lifecycleScope.launch {
                apiService.createCommande(request) { success, commande, message ->
                    runOnUiThread {
                        btnCreer.isEnabled = true
                        btnCreer.text = "Créer"

                        if (success) {
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Commande créée", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadUserData() // Recharger les stats
                        } else {
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Erreur de création", Toast.LENGTH_SHORT).show()
                        }
                    }
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
            val dureeStr = etDureeEstimee.text.toString().trim()
            val budgetStr = etBudget.text.toString().trim()

            if (description.isEmpty() || adresse.isEmpty() || dateSouhaitee.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duree = try {
                if (dureeStr.isEmpty()) 60 else dureeStr.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Durée invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = try {
                if (budgetStr.isEmpty()) 0.0 else budgetStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Budget invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            // Utiliser la vraie API
            val request = PrestationRequest(
                titre = typePrestation,
                description = description,
                tarif = budget,
                date_prestation = formatDateForApi(dateSouhaitee),
                duree_estimee = duree,
                adresse = adresse
            )

            lifecycleScope.launch {
                apiService.createPrestation(request) { success, prestation, message ->
                    runOnUiThread {
                        btnCreer.isEnabled = true
                        btnCreer.text = "Créer"

                        if (success) {
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Prestation créée", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadUserData() // Recharger les stats
                        } else {
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Erreur de création", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnAnnuler.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun formatDateForApi(dateString: String): String {
        return try {
            // Convertir du format dd/MM/yyyy vers ISO
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // En cas d'erreur, utiliser la date actuelle
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}