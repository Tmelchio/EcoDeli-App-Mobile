package com.ecodeli

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnConnexion: Button
    private lateinit var btnInscription: Button
    private lateinit var btnFooterMenu: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnConnexion = findViewById(R.id.btnConnexion)
        btnInscription = findViewById(R.id.btnInscription)
        btnFooterMenu = findViewById(R.id.btnFooterMenu)

        btnConnexion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnFooterMenu.setOnClickListener {
            showFooterPopup()
        }
    }

    private fun showFooterPopup() {
        val builder = AlertDialog.Builder(this, R.style.TransparentDialog)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_menu, null)
        builder.setView(view)
        val dialog = builder.create()

        // Click listeners
        view.findViewById<TextView>(R.id.menuProfil)?.setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuClient)?.setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuLivreur)?.setOnClickListener {
        }
            dialog.dismiss()

        view.findViewById<TextView>(R.id.menuNotif)?.setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuParam)?.setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuLogout)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()


        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val params = dialog.window?.attributes
        params?.y = 500
        dialog.window?.attributes = params
    }
}