package com.ecodeli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var nfcButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.btnLogin)
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        nfcButton = findViewById(R.id.nfcButton)

        btnLogin.setOnClickListener {
            // Logique de connexion classique ici
        }

        nfcButton.setOnClickListener {
            val intent = Intent(this, NfcLoginActivity::class.java)
            startActivity(intent)
        }
    }
}