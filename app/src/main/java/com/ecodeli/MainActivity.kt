package com.ecodeli

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirige vers la page de connexion
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}