package com.ecodeli

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.models.api.ProductRequest
import com.ecodeli.models.api.ServiceRequest
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.ecodeli.models.api.LocationData


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
            showCreateProductDialog()
        }

        btnDemanderPrestation.setOnClickListener {
            showServiceTypesDialog()
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
            // Charger les demandes de produits pour calculer les stats
            apiService.getCommandes { success, productRequests, _ ->
                if (success && productRequests != null) {
                    tvTotalCommandes.text = productRequests.size.toString()
                    val totalCommandesDepense = productRequests.sumOf {
                        (it.product?.price ?: 0.0) * it.amount
                    }

                    // Charger les services
                    lifecycleScope.launch {
                        apiService.getPrestations { successP, services, _ ->
                            val totalPrestations = if (successP && services != null) {
                                services.size
                            } else 0

                            val totalPrestationsDepense = if (successP && services != null) {
                                services.sumOf { it.price }
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

    private fun showCreateProductDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_product, null)
        builder.setView(view)

        val etProductName = view.findViewById<EditText>(R.id.etProductName)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val spinnerSize = view.findViewById<Spinner>(R.id.spinnerSize)
        val etCity = view.findViewById<EditText>(R.id.etCity)
        val etZipcode = view.findViewById<EditText>(R.id.etZipcode)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val btnCreer = view.findViewById<Button>(R.id.btnCreer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)

        // Configurer le spinner des tailles
        val sizes = arrayOf("S (petit)", "M (moyen)", "L (grand)", "XL (très grand)", "XXL (énorme)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSize.adapter = adapter
        spinnerSize.setSelection(1) // M par défaut

        val dialog = builder.create()

        btnCreer.setOnClickListener {
            val productName = etProductName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val selectedSize = spinnerSize.selectedItemPosition + 1 // 1=S, 2=M, etc.
            val city = etCity.text.toString().trim()
            val zipcode = etZipcode.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (productName.isEmpty() || priceStr.isEmpty() || city.isEmpty() || zipcode.isEmpty() || address.isEmpty()) {
                Toast.makeText(this@ClientDashboardActivity, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = try {
                priceStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this@ClientDashboardActivity, "Prix invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            lifecycleScope.launch {
                // D'abord créer la location
                apiService.createLocation(city, zipcode, address) { success, location, message ->
                    if (success && location != null) {
                        // Ensuite créer le produit
                        val productRequest = ProductRequest(
                            name = productName,
                            price = price,
                            size = selectedSize,
                            location = LocationData(
                                city = location.city,
                                zipcode = location.zipcode,
                                address = location.address
                            )
                        )


                        lifecycleScope.launch {
                            apiService.createProduct(productRequest) { productSuccess, product, productMessage ->
                                runOnUiThread {
                                    btnCreer.isEnabled = true
                                    btnCreer.text = "Créer"

                                    if (productSuccess) {
                                        Toast.makeText(this@ClientDashboardActivity, "Produit créé avec succès !", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                        loadUserData()
                                    } else {
                                        Toast.makeText(this@ClientDashboardActivity, productMessage ?: "Erreur création produit", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            btnCreer.isEnabled = true
                            btnCreer.text = "Créer"
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Erreur création adresse", Toast.LENGTH_SHORT).show()
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

    private fun showServiceTypesDialog() {
        val serviceTypes = arrayOf(
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
            .setItems(serviceTypes) { _, which ->
                showCreateServiceDialog(serviceTypes[which])
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showCreateServiceDialog(typeService: String) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_service, null)
        builder.setView(view)

        val tvServiceType = view.findViewById<TextView>(R.id.tvServiceType)
        val etServiceDescription = view.findViewById<EditText>(R.id.etServiceDescription)
        val etServiceDate = view.findViewById<EditText>(R.id.etServiceDate)
        val etServicePrice = view.findViewById<EditText>(R.id.etServicePrice)
        val btnCreer = view.findViewById<Button>(R.id.btnCreer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)

        tvServiceType.text = typeService

        val dialog = builder.create()

        btnCreer.setOnClickListener {
            val description = etServiceDescription.text.toString().trim()
            val dateStr = etServiceDate.text.toString().trim()
            val priceStr = etServicePrice.text.toString().trim()

            if (description.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this@ClientDashboardActivity, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = try {
                if (priceStr.isEmpty()) 0.0 else priceStr.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this@ClientDashboardActivity, "Prix invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreer.isEnabled = false
            btnCreer.text = "Création..."

            val serviceRequest = ServiceRequest(
                name = typeService,
                description = description,
                price = price,
                date = formatDateForApi(dateStr)
            )

            lifecycleScope.launch {
                apiService.createService(serviceRequest) { success, service, message ->
                    runOnUiThread {
                        btnCreer.isEnabled = true
                        btnCreer.text = "Créer"

                        if (success) {
                            Toast.makeText(this@ClientDashboardActivity, "Service demandé avec succès !", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadUserData()
                        } else {
                            Toast.makeText(this@ClientDashboardActivity, message ?: "Erreur création service", Toast.LENGTH_SHORT).show()
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