package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.CommandeResponse
import com.ecodeli.models.api.PrestationResponse

// ==================== ADAPTER COMMANDES API ====================
class CommandeAdapterReal(
    private val commandes: List<CommandeResponse>,
    private val onItemClick: (CommandeResponse) -> Unit
) : RecyclerView.Adapter<CommandeAdapterReal.CommandeViewHolder>() {

    class CommandeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommercant: TextView = itemView.findViewById(R.id.tvCommercant)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvMontant: TextView = itemView.findViewById(R.id.tvMontant)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTypeService: TextView = itemView.findViewById(R.id.tvTypeService)
        val tvUrgent: TextView? = itemView.findViewById(R.id.tvUrgent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_commande, parent, false)
        return CommandeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommandeViewHolder, position: Int) {
        val commande = commandes[position]

        holder.tvCommercant.text = commande.commercant
        holder.tvDescription.text = commande.description
        holder.tvMontant.text = String.format("%.2f€", commande.montant)
        holder.tvStatus.text = getStatusLabel(commande.status)
        holder.tvDate.text = formatDate(commande.date_commande)
        holder.tvTypeService.text = "📦 Livraison"

        // Masquer le badge urgent par défaut
        holder.tvUrgent?.visibility = View.GONE

        // Couleur du statut
        holder.tvStatus.setTextColor(getStatusColor(holder.itemView.context, commande.status))

        holder.itemView.setOnClickListener {
            onItemClick(commande)
        }
    }

    override fun getItemCount() = commandes.size

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "en_attente" -> "En attente"
            "en_livraison" -> "En livraison"
            "livree" -> "Livrée"
            "validee" -> "Validée"
            "annulee" -> "Annulée"
            else -> status
        }
    }

    private fun getStatusColor(context: android.content.Context, status: String): Int {
        return when (status) {
            "en_attente" -> context.getColor(android.R.color.holo_orange_dark)
            "en_livraison" -> context.getColor(android.R.color.holo_blue_dark)
            "livree" -> context.getColor(R.color.ecodeli_green)
            "validee" -> context.getColor(android.R.color.darker_gray)
            "annulee" -> context.getColor(android.R.color.holo_red_dark)
            else -> context.getColor(android.R.color.black)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Format simple : YYYY-MM-DD -> DD/MM/YYYY
            val parts = dateString.substring(0, 10).split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                dateString.substring(0, 10)
            }
        } catch (e: Exception) {
            dateString
        }
    }
}

// ==================== ADAPTER PRESTATIONS API ====================
class PrestationAdapterReal(
    private val prestations: List<PrestationResponse>,
    private val onItemClick: (PrestationResponse) -> Unit
) : RecyclerView.Adapter<PrestationAdapterReal.PrestationViewHolder>() {

    class PrestationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitre: TextView = itemView.findViewById(R.id.tvTitre)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvTarif: TextView = itemView.findViewById(R.id.tvTarif)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDuree: TextView = itemView.findViewById(R.id.tvDuree)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrestationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prestation, parent, false)
        return PrestationViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrestationViewHolder, position: Int) {
        val prestation = prestations[position]

        holder.tvTitre.text = prestation.titre
        holder.tvDescription.text = prestation.description
        holder.tvTarif.text = String.format("%.2f€", prestation.tarif)
        holder.tvStatus.text = getStatusLabel(prestation.status)
        holder.tvDate.text = formatDate(prestation.date_prestation)
        holder.tvDuree.text = "${prestation.duree_estimee} min"

        // Couleur du statut
        holder.tvStatus.setTextColor(getStatusColor(holder.itemView.context, prestation.status))

        holder.itemView.setOnClickListener {
            onItemClick(prestation)
        }
    }

    override fun getItemCount() = prestations.size

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "demandee" -> "Demandée"
            "acceptee" -> "Acceptée"
            "en_cours" -> "En cours"
            "terminee" -> "Terminée"
            "annulee" -> "Annulée"
            else -> status
        }
    }

    private fun getStatusColor(context: android.content.Context, status: String): Int {
        return when (status) {
            "demandee" -> context.getColor(android.R.color.holo_orange_dark)
            "acceptee" -> context.getColor(android.R.color.holo_blue_dark)
            "en_cours" -> context.getColor(R.color.ecodeli_green)
            "terminee" -> context.getColor(android.R.color.darker_gray)
            "annulee" -> context.getColor(android.R.color.holo_red_dark)
            else -> context.getColor(android.R.color.black)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Format simple : YYYY-MM-DD -> DD/MM/YYYY
            val parts = dateString.substring(0, 10).split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                dateString.substring(0, 10)
            }
        } catch (e: Exception) {
            dateString
        }
    }
}