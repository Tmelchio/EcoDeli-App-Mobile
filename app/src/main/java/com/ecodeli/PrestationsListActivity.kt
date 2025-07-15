package com.ecodeli

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.adapters.PrestationAdapterReal
import com.ecodeli.models.api.ServiceResponse
import com.ecodeli.services.RealApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class PrestationsListActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: RealApiService
    private lateinit var rvPrestations: RecyclerView
    private lateinit var prestationAdapter: PrestationAdapterReal

    private val prestationsList = mutableListOf<ServiceResponse>()
    private var userId: String = ""

    companion object {
        private const val TAG = "PrestationsListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestations_list)

        prefs = getSharedPreferences("ecodeli_prefs", MODE_PRIVATE)
        apiService = RealApiService(this)
        userId = prefs.getString("user_id", "") ?: ""

        Log.d(TAG, "Initialisation avec userId: $userId")

        initViews()
        setupRecyclerView()
        loadPrestations()
    }

    private fun initViews() {
        rvPrestations = findViewById(R.id.rvPrestations)

        // Bouton retour dans la toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mes Prestations"

        Log.d(TAG, "Vues initialisées")
    }

    private fun setupRecyclerView() {
        prestationAdapter = PrestationAdapterReal(prestationsList) { prestation ->
            Log.d(TAG, "Clic sur prestation: ${prestation._id}")
            showPrestationDetails(prestation)
        }

        rvPrestations.apply {
            layoutManager = LinearLayoutManager(this@PrestationsListActivity)
            adapter = prestationAdapter
        }

        Log.d(TAG, "RecyclerView configuré")
    }

    private fun loadPrestations() {
        Log.d(TAG, "Début chargement prestations")

        lifecycleScope.launch {
            apiService.getPrestations { success, prestations, message ->
                runOnUiThread {
                    if (success && prestations != null) {
                        Log.d(TAG, "Prestations chargées avec succès: ${prestations.size}")

                        // Debug des données reçues
                        prestations.forEachIndexed { index, service ->
                            Log.d(TAG, "Prestation $index: ID=${service._id}, name=${service.name}, user=${service.user?.javaClass}, actor=${service.actor?.javaClass}")
                        }

                        prestationsList.clear()
                        prestationsList.addAll(prestations)
                        prestationAdapter.notifyDataSetChanged()

                        Log.d(TAG, "Adapter notifié, ${prestationsList.size} éléments")
                    } else {
                        Log.e(TAG, "Erreur chargement prestations: $message")
                        Toast.makeText(this@PrestationsListActivity,
                            message ?: "Erreur lors du chargement",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showPrestationDetails(prestation: ServiceResponse) {
        Log.d(TAG, "Affichage détails prestation: ${prestation._id}")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Détails de la prestation")

        // Extraire les informations de manière sécurisée
        val actorInfo = extractActorInfo(prestation.actor)
        val userInfo = extractUserInfo(prestation.user)
        val status = if (actorInfo.isAssigned) "Assigné" else "En attente"

        Log.d(TAG, "Infos extraites - Actor: ${actorInfo.name}, User: ${userInfo.name}, Status: $status")

        val message = """
            Service: ${prestation.name}
            Description: ${prestation.description}
            Tarif: ${String.format("%.2f€", prestation.price)}
            Statut: $status
            Prestataire: ${actorInfo.name}
            Demandeur: ${userInfo.name}
            Date souhaitée: ${formatDate(prestation.date)}
            Date création: ${formatDate(prestation.creation_date)}
        """.trimIndent()

        builder.setMessage(message)

        // Actions selon le statut
        if (!actorInfo.isAssigned) {
            builder.setPositiveButton("Annuler la demande") { _, _ ->
                cancelPrestation(prestation)
            }
        } else {
            builder.setPositiveButton("Contacter prestataire") { _, _ ->
                contactPrestataire(prestation, actorInfo)
            }
        }

        builder.setNegativeButton("Fermer", null)

        val dialog = builder.create()
        dialog.show()

        Log.d(TAG, "Dialog affiché")
    }

    private fun cancelPrestation(prestation: ServiceResponse) {
        Log.d(TAG, "Annulation prestation: ${prestation._id}")

        AlertDialog.Builder(this)
            .setTitle("Annuler la prestation")
            .setMessage("Êtes-vous sûr de vouloir annuler cette prestation ?")
            .setPositiveButton("Oui") { _, _ ->
                lifecycleScope.launch {
                    apiService.cancelPrestation(prestation._id) { success, message ->
                        runOnUiThread {
                            if (success) {
                                Log.d(TAG, "Prestation annulée avec succès")
                                Toast.makeText(this@PrestationsListActivity,
                                    "Prestation annulée",
                                    Toast.LENGTH_SHORT).show()
                                loadPrestations() // Recharger la liste
                            } else {
                                Log.e(TAG, "Erreur annulation: $message")
                                Toast.makeText(this@PrestationsListActivity,
                                    message ?: "Erreur",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun contactPrestataire(prestation: ServiceResponse, actorInfo: ActorInfo) {
        Log.d(TAG, "Contact prestataire: ${actorInfo.name}")

        // Pour l'instant, juste un message d'information
        // Plus tard, on pourrait implémenter un système de chat ou d'email
        AlertDialog.Builder(this)
            .setTitle("Contacter ${actorInfo.name}")
            .setMessage("Fonctionnalité de contact en cours de développement.\n\nVous pouvez contacter votre prestataire ${actorInfo.name} pour la prestation \"${prestation.name}\".")
            .setPositiveButton("OK", null)
            .show()
    }

    // ==================== MÉTHODES HELPER ====================

    private data class ActorInfo(
        val name: String,
        val isAssigned: Boolean,
        val email: String? = null
    )

    private data class UserInfo(
        val name: String,
        val email: String? = null
    )

    private fun extractActorInfo(actorField: Any?): ActorInfo {
        return try {
            when (actorField) {
                is JsonObject -> {
                    val actorObj = actorField as JsonObject
                    val firstname = actorObj.get("firstname")?.asString ?: ""
                    val name = actorObj.get("name")?.asString ?: ""
                    val email = actorObj.get("email")?.asString
                    val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Prestataire"
                    }

                    Log.d(TAG, "ActorInfo extrait (JsonObject): $fullName, email=$email")
                    ActorInfo(fullName, true, email)
                }
                is Map<*, *> -> {
                    val actorMap = actorField as Map<*, *>
                    val firstname = actorMap["firstname"] as? String ?: ""
                    val name = actorMap["name"] as? String ?: ""
                    val email = actorMap["email"] as? String
                    val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Prestataire"
                    }

                    Log.d(TAG, "ActorInfo extrait (Map): $fullName, email=$email")
                    ActorInfo(fullName, true, email)
                }
                is Number -> {
                    val actorId = actorField.toInt()
                    Log.d(TAG, "Actor ID trouvé: $actorId")
                    ActorInfo("Prestataire (ID: $actorId)", true)
                }
                null -> {
                    Log.d(TAG, "Aucun actor assigné")
                    ActorInfo("Aucun prestataire assigné", false)
                }
                else -> {
                    Log.w(TAG, "Actor field type inconnu: ${actorField.javaClass}")
                    ActorInfo("Aucun prestataire assigné", false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info actor", e)
            ActorInfo("Aucun prestataire assigné", false)
        }
    }

    private fun extractUserInfo(userField: Any?): UserInfo {
        return try {
            when (userField) {
                is JsonObject -> {
                    val userObj = userField as JsonObject
                    val firstname = userObj.get("firstname")?.asString ?: ""
                    val name = userObj.get("name")?.asString ?: ""
                    val email = userObj.get("email")?.asString
                    val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Utilisateur"
                    }

                    Log.d(TAG, "UserInfo extrait (JsonObject): $fullName, email=$email")
                    UserInfo(fullName, email)
                }
                is Map<*, *> -> {
                    val userMap = userField as Map<*, *>
                    val firstname = userMap["firstname"] as? String ?: ""
                    val name = userMap["name"] as? String ?: ""
                    val email = userMap["email"] as? String
                    val fullName = if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Utilisateur"
                    }

                    Log.d(TAG, "UserInfo extrait (Map): $fullName, email=$email")
                    UserInfo(fullName, email)
                }
                is Number -> {
                    val userId = userField.toInt()
                    Log.d(TAG, "User ID trouvé: $userId")
                    UserInfo("Utilisateur (ID: $userId)")
                }
                null -> {
                    Log.d(TAG, "Aucun user")
                    UserInfo("Utilisateur non spécifié")
                }
                else -> {
                    Log.w(TAG, "User field type inconnu: ${userField.javaClass}")
                    UserInfo("Utilisateur non spécifié")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction info user", e)
            UserInfo("Utilisateur non spécifié")
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Simplifier l'affichage de la date
            if (dateString.contains("T")) {
                // Format ISO: 2025-01-15T14:30:00.000Z -> 15/01/2025
                val datePart = dateString.substring(0, 10)
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    datePart.replace("-", "/")
                }
            } else if (dateString.contains("-")) {
                // Format: 2025-01-15 -> 15/01/2025
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    dateString.replace("-", "/")
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur formatage date: $dateString", e)
            dateString
        }
    }

    private fun getStatusText(isAssigned: Boolean): String {
        return if (isAssigned) "Assigné" else "En attente"
    }

    private fun getStatusColor(isAssigned: Boolean): Int {
        return if (isAssigned) {
            android.R.color.holo_green_light
        } else {
            android.R.color.holo_orange_light
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "Navigation retour")
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed - rechargement des prestations")
        loadPrestations()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity détruite")
    }
}