package com.ecodeli

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NfcDetectionActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_detection)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC non supporté sur cet appareil", Toast.LENGTH_LONG).show()
            finish()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Activez le NFC dans les paramètres", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf<android.content.IntentFilter>()
        val techList = arrayOf<Array<String>>()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techList)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            val tagId = tag?.id?.joinToString("") { String.format("%02X", it) }

            Toast.makeText(this, "Carte détectée: $tagId", Toast.LENGTH_LONG).show()

            // Rediriger vers CardInfoActivity
            val cardInfoIntent = Intent(this, CardInfoActivity::class.java)
            cardInfoIntent.putExtra("cardOwner", "Inconnu") // À adapter si tu lis la carte
            cardInfoIntent.putExtra("cardId", tagId)
            startActivity(cardInfoIntent)
        }
    }
}
