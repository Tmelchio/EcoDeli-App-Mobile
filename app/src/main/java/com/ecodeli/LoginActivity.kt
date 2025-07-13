package com.ecodeli

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecodeli.services.ApiService

class LoginActivity : AppCompatActivity() {

    private lateinit var btnConnexion: Button
    private lateinit var btnInscription: Button
    private lateinit var nfcButton: ImageView
    private lateinit var btnLogin: Button
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var layoutConnexion: LinearLayout
    private lateinit var layoutInscription: LinearLayout

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialiser les services
        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = ApiService()

        // Vérifier si l'utilisateur est déjà connecté
        if (prefs.getBoolean("is_logged_in", false)) {
            redirectToMainActivity()
            return
        }

        // Initialiser les vues
        initViews()
        setupClickListeners()

        // Afficher le formulaire de connexion par défaut
        showLoginForm()
    }

    private fun initViews() {
        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)
        nfcButton = findViewById(R.id.nfcButton)
        btnLogin = findViewById(R.id.btnLogin)
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        layoutConnexion = findViewById(R.id.layoutConnexion)
        layoutInscription = findViewById(R.id.layoutInscription)
    }

    private fun setupClickListeners() {
        btnInscription.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnConnexion.setOnClickListener {
            showLoginForm()
        }

        btnLogin.setOnClickListener {
            performLogin()
        }

        nfcButton.setOnClickListener {
            handleNFCLogin()
        }
    }

    private fun showLoginForm() {
        layoutConnexion.visibility = View.VISIBLE
        layoutInscription.visibility = View.GONE

        // Mettre à jour les styles des boutons
        btnConnexion.setBackgroundResource(R.drawable.btn_green_border)
        btnConnexion.setTextColor(getColor(android.R.color.white))

        btnInscription.setBackgroundResource(R.drawable.btn_green)
        btnInscription.setTextColor(getColor(android.R.color.black))
    }

    private fun performLogin() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        // Validation des champs
        if (email.isEmpty()) {
            loginEmail.error = "Email requis"
            return
        }

        if (password.isEmpty()) {
            loginPassword.error = "Mot de passe requis"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.error = "Email invalide"
            return
        }

        // Appel API
        btnLogin.isEnabled = false
        btnLogin.text = "Connexion..."

        apiService.login(email, password) { success, userType, message ->
            runOnUiThread {
                btnLogin.isEnabled = true
                btnLogin.text = "Se connecter"

                if (success) {
                    // Sauvegarder la session - TOUJOURS CLIENT pour Mission 2
                    prefs.edit().apply {
                        putBoolean("is_logged_in", true)
                        putString("user_email", email)
                        putString("user_type", "client")
                        putString("user_id", "user_${System.currentTimeMillis()}")
                        apply()
                    }

                    Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                    redirectToMainActivity()
                } else {
                    Toast.makeText(this, message ?: "Erreur de connexion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleNFCLogin() {
        // Simulation de connexion NFC
        Toast.makeText(this, "Approchez votre carte NFC...", Toast.LENGTH_SHORT).show()

        // Simuler une connexion NFC après 2 secondes
        nfcButton.postDelayed({
            // Simulation connexion client via NFC
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_email", "client@nfc.ecodeli")
                putString("user_type", "client")
                putString("user_id", "nfc_user_${System.currentTimeMillis()}")
                apply()
            }

            Toast.makeText(this, "Connexion NFC réussie !", Toast.LENGTH_SHORT).show()
            redirectToMainActivity()
        }, 2000)
    }

    private fun redirectToMainActivity() {
        // TOUJOURS rediriger vers ClientDashboard pour Mission 2
        val intent = Intent(this, ClientDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}