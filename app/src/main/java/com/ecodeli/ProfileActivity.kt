package com.ecodeli

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)

        initViews()
        setupClickListeners()
        loadUserInfo()
    }

    private fun initViews() {
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            performLogout()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserInfo() {
        val email = prefs.getString("user_email", "Non connecté") ?: "Non connecté"
        val firstname = prefs.getString("user_firstname", "") ?: ""
        val name = prefs.getString("user_name", "") ?: ""
        val role = prefs.getString("user_role", "Client") ?: "Client"
        val subscription = prefs.getString("user_subscription", "") ?: ""

        // Afficher le nom complet si disponible, sinon l'email
        val displayText = if (firstname.isNotEmpty() && name.isNotEmpty()) {
            "$firstname $name\n$email"
        } else {
            email
        }

        tvUserEmail.text = displayText

        // Afficher des informations supplémentaires si vous ajoutez des TextViews dans le layout
        // findViewById<TextView>(R.id.tvUserRole)?.text = "Rôle: $role"
        // if (subscription.isNotEmpty()) {
        //     findViewById<TextView>(R.id.tvUserSubscription)?.text = "Abonnement: $subscription"
        // }
    }

    private fun performLogout() {
        // Désactiver le bouton pendant la déconnexion
        btnLogout.isEnabled = false
        btnLogout.text = "Déconnexion..."

        lifecycleScope.launch {
            apiService.logout { success, message ->
                runOnUiThread {
                    btnLogout.isEnabled = true
                    btnLogout.text = "Déconnexion"

                    if (success) {
                        Toast.makeText(this@ProfileActivity, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Erreur de déconnexion: $message", Toast.LENGTH_SHORT).show()
                    }

                    // Rediriger vers la page de connexion dans tous les cas
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Vous pouvez améliorer le formatage de date ici
            dateString.substring(0, 10).replace("-", "/")
        } catch (e: Exception) {
            dateString
        }
    }
}