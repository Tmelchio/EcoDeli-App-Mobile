package com.ecodeli

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: Button
    private lateinit var btnRegisterBadge: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)

        initViews()
        setupClickListeners()
        loadUserInfo()
    }

    private fun initViews() {
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        btnRegisterBadge = findViewById(R.id.btnRegisterBadge)
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener { performLogout() }
        btnBack.setOnClickListener { finish() }
        btnRegisterBadge.setOnClickListener {
            val intent = Intent(this, NfcRegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserInfo() {
        val email = prefs.getString("user_email", "Non connecté")
        tvUserEmail.text = email
    }

    private fun performLogout() {
        prefs.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}