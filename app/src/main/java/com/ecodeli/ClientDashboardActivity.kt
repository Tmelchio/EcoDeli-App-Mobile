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
import com.ecodeli.adapters.CommandeAdapter
import com.ecodeli.models.Commande
import com.ecodeli.models.Prestation
import com.ecodeli.services.ApiService

class ClientDashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService

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
        apiService = ApiService()

        // Récupérer les données utilisateur
        userId = prefs.getString("user_id", "") ?: ""
        userEmail = prefs.getString("user_email", "") ?: ""

        // Initialiser les vues
        initViews()
        setupClickListeners()
        setupRecyclerView()

        // Charger les données
        loadUserStats()
        loadRecentActivities()
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
        tvWelcome.text = "Bienvenue, $userEmail"
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

        // Menu profil simplifié - CORRECTION ICI
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

    private fun loadUserStats() {
        apiService.getClientStats(userId) { stats ->
            runOnUiThread {
                tvTotalCommandes.text = stats["total_commandes"].toString()
                tvTotalPrestations.text = stats["total_prestations"].toString()
                tvTotalDepense.text = "${stats["total_depense"]}€"
            }
        }
    }

    private fun loadRecentActivities() {
        apiService.getClientCommandes(userId) { commandes ->
            runOnUiThread {
                commandesList.clear()
                // Prendre les 5 dernières commandes
                commandesList.addAll(commandes.take(5))
                activiteAdapter.notifyDataSetChanged()
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

            val nouvelleCommande = Commande(
                id = "",
                clientId = userId,
                commercant = commercant,
                description = description,
                montant = montant,
                status = "en_attente",
                adresseLivraison = adresse
            )

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            apiService.createCommande(nouvelleCommande) { success, message ->
                runOnUiThread {
                    btnCreer.isEnabled = true
                    btnCreer.text = "Créer"

                    if (success) {
                        Toast.makeText(this, "Commande créée avec succès !", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadRecentActivities()
                        loadUserStats()
                    } else {
                        Toast.makeText(this, message ?: "Erreur de création", Toast.LENGTH_SHORT).show()
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

            val nouvellePrestation = Prestation(
                id = "",
                prestataireId = "",
                clientId = userId,
                titre = typePrestation,
                description = description,
                tarif = budget,
                status = "demandee",
                datePrestation = System.currentTimeMillis(),
                dureeEstimee = duree,
                adresse = adresse
            )

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            apiService.createPrestation(nouvellePrestation) { success, message ->
                runOnUiThread {
                    btnCreer.isEnabled = true
                    btnCreer.text = "Créer"

                    if (success) {
                        Toast.makeText(this, "Demande de prestation créée !", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadUserStats()
                    } else {
                        Toast.makeText(this, message ?: "Erreur de création", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnAnnuler.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCommandeDetails(commande: Commande) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la commande")

        val message = """
            Commerçant: ${commande.commercant}
            Description: ${commande.description}
            Montant: ${commande.montant}€
            Statut: ${commande.status}
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

        val input = EditText(this)
        input.hint = "Code de validation"
        builder.setView(input)

        builder.setPositiveButton("Valider") { _, _ ->
            val code = input.text.toString().trim()
            if (code.isNotEmpty()) {
                apiService.validateLivraison(commande.id, code) { success, message ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Livraison validée avec le code: $code", Toast.LENGTH_SHORT).show()
                            loadRecentActivities()
                        } else {
                            Toast.makeText(this, message ?: "Erreur de validation", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Code requis", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        loadUserStats()
        loadRecentActivities()
    }
}