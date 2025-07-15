package com.ecodeli.utils

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import org.json.JSONObject
import java.nio.charset.Charset

class NfcManager(private val context: Context) {

    companion object {
        private const val TAG = "NfcManager"
    }

    // Créer les données à écrire sur la carte NFC
    fun createUserNfcData(userId: String, email: String, token: String): String {
        val userData = JSONObject().apply {
            put("userId", userId)
            put("email", email)
            put("token", token)
            put("timestamp", System.currentTimeMillis())
            put("app", "ecodeli")
        }
        return userData.toString()
    }

    // Écrire sur la carte NFC
    fun writeUserDataToTag(tag: Tag, userData: String): Boolean {
        return try {
            val ndefRecord = createNdefRecord(userData)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))
            writeNdefMessage(tag, ndefMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur écriture NFC", e)
            false
        }
    }

    // Lire depuis la carte NFC
    fun readUserDataFromTag(tag: Tag): String? {
        return try {
            val ndef = Ndef.get(tag)
            ndef?.connect()

            val ndefMessage = ndef?.ndefMessage
            val records = ndefMessage?.records

            if (records != null && records.isNotEmpty()) {
                val payload = records[0].payload

                // Skip les 3 premiers octets pour le format text
                val languageCodeLength = payload[0].toInt() and 0x3f
                val textBytes = payload.copyOfRange(1 + languageCodeLength, payload.size)

                String(textBytes, Charset.forName("UTF-8"))
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lecture NFC", e)
            null
        }
    }

    // Valider les données lues
    fun validateNfcData(nfcData: String): NfcUserData? {
        return try {
            val jsonObject = JSONObject(nfcData)

            val userId = jsonObject.optString("userId")
            val email = jsonObject.optString("email")
            val token = jsonObject.optString("token")
            val timestamp = jsonObject.optLong("timestamp")
            val app = jsonObject.optString("app")

            // Vérifier que c'est bien une carte EcoDeli
            if (app != "ecodeli" || userId.isEmpty() || email.isEmpty()) {
                return null
            }

            // Vérifier que la carte n'est pas trop ancienne (30 jours)
            val maxAge = 30L * 24 * 60 * 60 * 1000
            if (System.currentTimeMillis() - timestamp > maxAge) {
                return null
            }

            NfcUserData(userId, email, token, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur validation NFC", e)
            null
        }
    }

    private fun createNdefRecord(data: String): NdefRecord {
        return NdefRecord.createTextRecord("en", data)
    }

    private fun writeNdefMessage(tag: Tag, ndefMessage: NdefMessage): Boolean {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(ndefMessage)
                    ndef.close()
                    true
                } else {
                    ndef.close()
                    false
                }
            } else {
                // Essayer de formater la carte
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    ndefFormatable.format(ndefMessage)
                    ndefFormatable.close()
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur écriture NDEF", e)
            false
        }
    }

    data class NfcUserData(
        val userId: String,
        val email: String,
        val token: String,
        val timestamp: Long
    )
}