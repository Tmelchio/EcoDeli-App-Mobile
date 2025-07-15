package com.ecodeli

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecodeli.utils.NfcManager

class NfcRegisterActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_register)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcManager = NfcManager(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC non disponible sur cet appareil", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Veuillez activer le NFC dans les paramètres", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            writeUserDataToTag(tag)
        }
    }

    private fun writeUserDataToTag(tag: Tag) {
        try {
            // Récupérer les données utilisateur stockées
            val prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
            val userId = prefs.getString("user_id", "") ?: ""
            val email = prefs.getString("user_email", "") ?: ""
            val token = prefs.getString("auth_token", "") ?: ""

            if (userId.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Données utilisateur manquantes", Toast.LENGTH_SHORT).show()
                return
            }

            // Créer les données NFC
            val userData = nfcManager.createUserNfcData(userId, email, token)

            // Écrire sur la carte
            val success = nfcManager.writeUserDataToTag(tag, userData)

            if (success) {
                Toast.makeText(this, "Badge enregistré avec succès !", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, NfcSuccessActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement du badge", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de l'enregistrement: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}