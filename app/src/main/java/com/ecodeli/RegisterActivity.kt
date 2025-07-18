package com.ecodeli

import android.content.Intent
<<<<<<< HEAD
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
=======
<<<<<<< HEAD
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.services.RealApiService
import kotlinx.coroutines.launch
=======
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnConnexion: Button
    private lateinit var btnInscription: Button
<<<<<<< HEAD
=======
<<<<<<< HEAD
    private lateinit var btnRegister: Button

    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etBirthDate: EditText

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
=======
>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

<<<<<<< HEAD
=======
<<<<<<< HEAD
        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)
        btnRegister = findViewById(R.id.btnRegister)

        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etBirthDate = findViewById(R.id.etBirthDate)
    }

    private fun setupClickListeners() {
        btnConnexion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegister.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val birthDate = etBirthDate.text.toString().trim()

        if (nom.isEmpty()) {
            etNom.error = "Nom requis"
            return
        }
        if (prenom.isEmpty()) {
            etPrenom.error = "Prénom requis"
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email requis"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email invalide"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Mot de passe requis"
            return
        }
        if (password.length < 6) {
            etPassword.error = "Mot de passe trop court (min 6 caractères)"
            return
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirmation requise"
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Les mots de passe ne correspondent pas"
            return
        }
        if (birthDate.isEmpty()) {
            etBirthDate.error = "Date de naissance requise"
            return
        }
        if (!isValidDate(birthDate)) {
            etBirthDate.error = "Format de date invalide (jj/mm/aaaa)"
            return
        }

        btnRegister.isEnabled = false
        btnRegister.text = "Inscription..."

        val userData = mapOf(
            "nom" to nom,
            "prenom" to prenom,
            "email" to email,
            "password" to password,
            "birthDate" to birthDate
        )

        lifecycleScope.launch {
            apiService.register(userData) { success, message ->
                btnRegister.isEnabled = true
                btnRegister.text = "S'inscrire"

                if (success) {
                    Toast.makeText(this@RegisterActivity, "Inscription réussie ! Veuillez vous connecter.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, message ?: "Erreur d'inscription", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val parts = date.split("/")
            if (parts.size != 3) return false
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            day in 1..31 && month in 1..12 && year in 1900..2024
        } catch (e: Exception) {
            false
        }
    }
}
=======
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d
        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)

        btnConnexion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
<<<<<<< HEAD
=======
>>>>>>> 7dab048 (Amélioration des interfaces de connexion et inscription)
>>>>>>> 00361081d63d1c9b5130dcc8c94d25d569fb1a9d
