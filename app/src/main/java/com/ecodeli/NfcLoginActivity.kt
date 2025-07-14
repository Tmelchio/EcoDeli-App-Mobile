package com.ecodeli

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NfcLoginActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_login)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC non disponible sur cet appareil", Toast.LENGTH_LONG).show()
            finish()
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
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val ndefMessage = ndef.ndefMessage
                    val records = ndefMessage?.records
                    val payload = records?.getOrNull(0)?.payload
                    val content = payload?.let {
                        // Ignore les 3 premiers octets si c'est du texte
                        if (records[0].toMimeType() == "text/plain" && it.size > 3)
                            it.copyOfRange(3, it.size).toString(Charsets.UTF_8)
                        else
                            it.toString(Charsets.UTF_8)
                    } ?: "Aucune donnée"
                    ndef.close()

                    Toast.makeText(this, "Contenu NFC : $content", Toast.LENGTH_LONG).show()
                    val homeIntent = Intent(this, ClientDashboardActivity::class.java)
                    startActivity(homeIntent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Erreur lecture NFC", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Tag NFC non compatible", Toast.LENGTH_SHORT).show()
            }
        }
    }
}