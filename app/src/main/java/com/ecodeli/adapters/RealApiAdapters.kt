package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.ProductRequestResponse
import com.ecodeli.models.api.ServiceResponse

// ==================== ADAPTER PRODUCT REQUESTS (ex-commandes) ====================
class CommandeAdapterReal(
    private val productRequests: List<ProductRequestResponse>,
    private val onItemClick: (ProductRequestResponse) -> Unit
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
        val productRequest = productRequests[position]
        val product = productRequest.product
        val seller = product?.seller

        // Utiliser les données du produit et vendeur
        holder.tvCommercant.text = if (seller != null) {
            "${seller.firstname} ${seller.name}"
        } else {
            "Vendeur"
        }

        holder.tvDescription.text = product?.name ?: "Produit"

        val totalPrice = (product?.price ?: 0.0) * productRequest.amount
        holder.tvMontant.text = String.format("%.2f€", totalPrice)

        holder.tvStatus.text = getStatusLabel(productRequest.delivery_status?.name ?: "pending")
        holder.tvDate.text = formatDate(productRequest.creation_date)
        holder.tvTypeService.text = "📦 Livraison"

        // Masquer le badge urgent par défaut
        holder.tvUrgent?.visibility = View.GONE

        // Couleur du statut
        holder.tvStatus.setTextColor(getStatusColor(holder.itemView.context, productRequest.delivery_status?.name ?: "pending"))

        holder.itemView.setOnClickListener {
            onItemClick(productRequest)
        }
    }

    override fun getItemCount() = productRequests.size

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "pending" -> "En attente"
            "accepted" -> "Acceptée"
            "in_progress" -> "En cours"
            "delivered" -> "Livrée"
            "cancelled" -> "Annulée"
            else -> status
        }
    }

    private fun getStatusColor(context: android.content.Context, status: String): Int {
        return when (status) {
            "pending" -> context.getColor(android.R.color.holo_orange_dark)
            "accepted" -> context.getColor(android.R.color.holo_blue_dark)
            "in_progress" -> context.getColor(android.R.color.holo_blue_dark)
            "delivered" -> context.getColor(R.color.ecodeli_green)
            "cancelled" -> context.getColor(android.R.color.holo_red_dark)
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

// ==================== ADAPTER SERVICES (ex-prestations) ====================
class PrestationAdapterReal(
    private val services: List<ServiceResponse>,
    private val onItemClick: (ServiceResponse) -> Unit
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
        val service = services[position]

        holder.tvTitre.text = service.name
        holder.tvDescription.text = service.description
        holder.tvTarif.text = String.format("%.2f€", service.price)
        holder.tvStatus.text = if (service.actor != null) "Assigné" else "En attente"
        holder.tvDate.text = formatDate(service.date)
        holder.tvDuree.text = "Service" // Pas de durée dans le modèle Service

        // Couleur du statut
        val statusColor = if (service.actor != null) {
            holder.itemView.context.getColor(R.color.ecodeli_green)
        } else {
            holder.itemView.context.getColor(android.R.color.holo_orange_dark)
        }
        holder.tvStatus.setTextColor(statusColor)

        holder.itemView.setOnClickListener {
            onItemClick(service)
        }
    }

    override fun getItemCount() = services.size

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