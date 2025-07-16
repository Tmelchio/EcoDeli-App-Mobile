package com.ecodeli

import android.app.PendingIntent
import android.content.Context
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

        // testUserId3() supprimé : on autorise la connexion avec n'importe quel compte
    }

    // testUserId3 supprimé : plus de restriction sur l'ID utilisateur

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
        val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
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
                // Afficher le contenu du badge pour debug
                showBadgeContent(nfcData)
                return
            }

            // Utiliser l'ID utilisateur pour la connexion via l'API
            lifecycleScope.launch {
                Toast.makeText(this@NfcLoginActivity, "Connexion en cours...", Toast.LENGTH_SHORT).show()
                
                // MODE NORMAL: Utiliser l'API avec l'endpoint /api/users
                apiService.loginWithUserId(userData.userId) { success, userType, message ->
                    if (success) {
                        Toast.makeText(this@NfcLoginActivity, "Connexion NFC réussie !", Toast.LENGTH_SHORT).show()
                        
                        // Rediriger vers le dashboard approprié
                        val homeIntent = Intent(this@NfcLoginActivity, ClientDashboardActivity::class.java)
                        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(homeIntent)
                        finish()
                    } else {
                        Toast.makeText(this@NfcLoginActivity, 
                            message ?: "Erreur de connexion NFC", 
                            Toast.LENGTH_LONG).show()
                        
                        // Si l'utilisateur n'est pas trouvé, afficher le contenu du badge
                        if (message?.contains("non trouvé") == true) {
                            showBadgeContent(nfcData)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la lecture NFC: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Version de test - connexion directe sans API
    private fun testDirectLogin(userId: String) {
        lifecycleScope.launch {
            Toast.makeText(this@NfcLoginActivity, "Test: Connexion directe avec ID=$userId", Toast.LENGTH_SHORT).show()
            
            // Simuler une connexion réussie
            val prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("user_id", userId)
            editor.putString("user_email", "test@ecodeli.com")
            editor.putString("user_firstname", "Test")
            editor.putString("user_name", "User")
            editor.putString("auth_token", "test_token_$userId")
            editor.apply()
            
            Toast.makeText(this@NfcLoginActivity, "✅ Connexion de test réussie !", Toast.LENGTH_SHORT).show()
            
            // Rediriger vers le dashboard
            val homeIntent = Intent(this@NfcLoginActivity, ClientDashboardActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)
            finish()
        }
    }

    private fun showBadgeContent(nfcData: String) {
        // Afficher le contenu du badge après un court délai
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000) // Attendre 2 secondes après le premier message
            
            // Utiliser un AlertDialog pour une meilleure lisibilité
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@NfcLoginActivity)
            builder.setTitle("Contenu du badge NFC")
            
            // Obtenir les informations de débogage formatées
            val debugInfo = nfcManager.getDebugInfo(nfcData)
            builder.setMessage(debugInfo)
            
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.setNegativeButton("Copier") { _, _ ->
                // Copier le contenu dans le presse-papiers
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Badge NFC Debug", debugInfo)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@NfcLoginActivity, "Contenu copié", Toast.LENGTH_SHORT).show()
            }
            builder.create().show()
        }
    }
}