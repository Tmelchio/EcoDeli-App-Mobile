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

    // Créer les données à écrire sur la carte NFC (uniquement l'ID utilisateur)
    fun createUserNfcData(userId: String): String {
        val userData = JSONObject().apply {
            put("userId", userId)
            put("app", "ecodeli")
            put("version", "1.0")
        }
        return userData.toString()
    }

    // Écrire sur la carte NFC
    fun writeUserDataToTag(tag: Tag, userData: String): Boolean {
        return try {
            // Vérifier que les données ne sont pas trop grandes
            if (userData.length > 8000) { // Limite de sécurité
                Log.e(TAG, "Données utilisateur trop grandes")
                return false
            }

            val ndefRecord = createNdefRecord(userData)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))
            writeNdefMessage(tag, ndefMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur écriture NFC: ${e.message}", e)
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

    // Valider les données lues (uniquement l'ID utilisateur)
    fun validateNfcData(nfcData: String): NfcUserData? {
        return try {
            val jsonObject = JSONObject(nfcData)

            val userId = jsonObject.optString("userId")
            val app = jsonObject.optString("app")

            // Vérifier que c'est bien une carte EcoDeli et qu'elle contient un ID
            if (app != "ecodeli" || userId.isEmpty()) {
                return null
            }

            // Pas de vérification de timestamp puisqu'on ne l'enregistre plus
            NfcUserData(userId, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Erreur validation NFC", e)
            null
        }
    }

    // Vider le badge NFC
    fun clearNfcTag(tag: Tag): Boolean {
        return try {
            val emptyRecord = NdefRecord.createTextRecord("en", "")
            val emptyMessage = NdefMessage(arrayOf(emptyRecord))
            writeNdefMessage(tag, emptyMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du vidage du badge NFC", e)
            false
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
                    if (ndef.maxSize >= ndefMessage.toByteArray().size) {
                        ndef.writeNdefMessage(ndefMessage)
                        ndef.close()
                        true
                    } else {
                        Log.e(TAG, "Taille du message trop grande pour la carte")
                        ndef.close()
                        false
                    }
                } else {
                    Log.e(TAG, "Carte NFC non inscriptible")
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
                    Log.e(TAG, "Carte NFC non formatable")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur écriture NDEF: ${e.message}", e)
            false
        }
    }

    data class NfcUserData(
        val userId: String,
        val timestamp: Long
    )

    // Méthode pour obtenir des informations de débogage
    fun getDebugInfo(nfcData: String): String {
        return try {
            val jsonObject = JSONObject(nfcData)
            val userId = jsonObject.optString("userId", "N/A")
            val app = jsonObject.optString("app", "N/A")
            val version = jsonObject.optString("version", "N/A")
            
            "INFORMATIONS DEBUG:\n" +
                    "• ID Utilisateur: $userId\n" +
                    "• Application: $app\n" +
                    "• Version: $version\n" +
                    "• Endpoint utilisé: /api/users/$userId\n" +
                    "• Données brutes: $nfcData"
        } catch (e: Exception) {
            "Erreur lors de l'analyse: ${e.message}\n\nDonnées brutes: $nfcData"
        }
    }
}