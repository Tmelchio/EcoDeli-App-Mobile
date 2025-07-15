package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.ServiceResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.util.Log

class PrestationAdapterReal(
    private val prestations: List<ServiceResponse>,
    private val onItemClick: (ServiceResponse) -> Unit
) : RecyclerView.Adapter<PrestationAdapterReal.PrestationViewHolder>() {

    private val gson = Gson()

    companion object {
        private const val TAG = "PrestationAdapterReal"
    }

    class PrestationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitre: TextView = itemView.findViewById(R.id.tvTitre)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvTarif: TextView = itemView.findViewById(R.id.tvTarif)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrestationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prestation, parent, false)
        return PrestationViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrestationViewHolder, position: Int) {
        val prestation = prestations[position]

        // Extraire le statut d'assignation
        val isAssigned = prestation.actor != null
        val statusText = if (isAssigned) "Assigné" else "En attente"

        // Extraire le nom du prestataire
        val prestataireInfo = extractPrestataireInfo(prestation.actor)

        holder.tvTitre.text = prestation.name
        holder.tvDescription.text = prestation.description
        holder.tvTarif.text = String.format("%.2f€", prestation.price)
        holder.tvDate.text = formatDate(prestation.date)

        // Ajouter le statut à la description
        val fullDescription = "${prestation.description}\n\nStatut: $statusText"
        if (isAssigned) {
            holder.tvDescription.text = "$fullDescription\nPrestataire: $prestataireInfo"
        } else {
            holder.tvDescription.text = fullDescription
        }

        holder.itemView.setOnClickListener { onItemClick(prestation) }
    }

    private fun extractPrestataireInfo(actorField: Any?): String {
        return try {
            when (actorField) {
                is JsonObject -> {
                    val actorObj = actorField as JsonObject
                    val firstname = actorObj.get("firstname")?.asString ?: ""
                    val name = actorObj.get("name")?.asString ?: ""
                    if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Prestataire"
                    }
                }
                is Map<*, *> -> {
                    val actorMap = actorField as Map<*, *>
                    val firstname = actorMap["firstname"] as? String ?: ""
                    val name = actorMap["name"] as? String ?: ""
                    if (firstname.isNotEmpty() && name.isNotEmpty()) {
                        "$firstname $name"
                    } else {
                        "Prestataire"
                    }
                }
                null -> "Aucun prestataire assigné"
                else -> {
                    Log.d(TAG, "Actor field type: ${actorField.javaClass}")
                    "Prestataire"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction prestataire", e)
            "Prestataire"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Simplifier l'affichage de la date
            if (dateString.contains("T")) {
                dateString.substring(0, 10).replace("-", "/")
            } else {
                dateString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur formatage date", e)
            dateString
        }
    }

    override fun getItemCount(): Int = prestations.size
}