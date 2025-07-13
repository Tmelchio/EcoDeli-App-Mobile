package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.Prestation
import java.text.SimpleDateFormat
import java.util.*

class PrestationAdapter(
    private val prestations: List<Prestation>,
    private val onItemClick: (Prestation) -> Unit
) : RecyclerView.Adapter<PrestationAdapter.PrestationViewHolder>() {

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
        holder.tvTarif.text = "${prestation.tarif}€"
        holder.tvStatus.text = getStatusLabel(prestation.status)
        holder.tvDate.text = formatDate(prestation.datePrestation)
        holder.tvDuree.text = "${prestation.dureeEstimee} min"

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

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}