package com.ecodeli

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecodeli.services.RealApiService
import com.ecodeli.utils.NfcManager
import kotlinx.coroutines.launch

class NfcLoginActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcManager: NfcManager
    private lateinit var apiService: RealApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_login)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcManager = NfcManager(this)
        apiService = RealApiService(this)

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
            handleNfcTag(tag)
        }
    }

    private fun handleNfcTag(tag: Tag) {
        try {
            // Lire les données de la carte NFC
            val nfcData = nfcManager.readUserDataFromTag(tag)

            if (nfcData == null) {
                Toast.makeText(this, "Impossible de lire la carte NFC", Toast.LENGTH_SHORT).show()
                return
            }

            // Valider les données
            val userData = nfcManager.validateNfcData(nfcData)

            if (userData == null) {
                Toast.makeText(this, "Carte NFC invalide ou expirée", Toast.LENGTH_SHORT).show()
                return
            }

            // Vérifier le token avec l'API
            lifecycleScope.launch {
                // Sauvegarder temporairement le token pour la validation
                val prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("auth_token", userData.token)
                editor.putString("user_id", userData.userId)
                editor.putString("user_email", userData.email)
                editor.apply()

                // Valider le token avec l'API
                apiService.validateToken { success, userInfo ->
                    if (success && userInfo != null) {
                        Toast.makeText(this@NfcLoginActivity, "Connexion NFC réussie !", Toast.LENGTH_SHORT).show()

                        // Rediriger vers le dashboard
                        val homeIntent = Intent(this@NfcLoginActivity, ClientDashboardActivity::class.java)
                        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(homeIntent)
                        finish()
                    } else {
                        // Token invalide, nettoyer et demander une nouvelle connexion
                        editor.clear().apply()
                        Toast.makeText(this@NfcLoginActivity, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_LONG).show()

                        val loginIntent = Intent(this@NfcLoginActivity, LoginActivity::class.java)
                        startActivity(loginIntent)
                        finish()
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la lecture NFC: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}