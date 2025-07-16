package com.ecodeli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
<<<<<<< HEAD
=======
<<<<<<< HEAD
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var nfcButton: ImageView
    private lateinit var btnInscription: Button
    private lateinit var apiService: RealApiService
=======
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var btnConnexion: Button
    private lateinit var btnInscription: Button
<<<<<<< HEAD
=======
>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

<<<<<<< HEAD
        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)

=======
<<<<<<< HEAD
        apiService = RealApiService(this)

        btnLogin = findViewById(R.id.btnLogin)
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        nfcButton = findViewById(R.id.nfcButton)
        btnInscription = findViewById(R.id.btnInscription)

        // Pré-remplir l'email si fourni depuis l'inscription
        intent.getStringExtra("email")?.let { email ->
            loginEmail.setText(email)
        }

        btnLogin.setOnClickListener {
            performLogin()
        }

=======
        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)

>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d
        btnInscription.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
<<<<<<< HEAD
    }
}
=======
<<<<<<< HEAD

        nfcButton.setOnClickListener {
            val intent = Intent(this, NfcLoginActivity::class.java)
            startActivity(intent)
        }

        // Vérifier si l'utilisateur est déjà connecté
        checkExistingSession()
    }

    private fun checkExistingSession() {
        lifecycleScope.launch {
            apiService.validateToken { success, userInfo ->
                if (success && userInfo != null) {
                    // L'utilisateur est déjà connecté, rediriger vers le dashboard
                    val intent = Intent(this@LoginActivity, ClientDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun performLogin() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

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

        // Désactiver le bouton pendant la connexion
        btnLogin.isEnabled = false
        btnLogin.text = "Connexion..."

        lifecycleScope.launch {
            apiService.login(email, password) { success, userType, message ->
                btnLogin.isEnabled = true
                btnLogin.text = "Se connecter"

                if (success) {
                    Toast.makeText(this@LoginActivity, "Connexion réussie !", Toast.LENGTH_SHORT).show()

                    // Rediriger vers le dashboard
                    val intent = Intent(this@LoginActivity, ClientDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, message ?: "Erreur de connexion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
=======
    }
}
>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d
