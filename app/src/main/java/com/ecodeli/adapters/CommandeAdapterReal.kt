package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.ProductRequestResponse

class CommandeAdapterReal(
    private val commandes: List<ProductRequestResponse>,
    private val onItemClick: (ProductRequestResponse) -> Unit
) : RecyclerView.Adapter<CommandeAdapterReal.CommandeViewHolder>() {

    class CommandeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommercant: TextView = itemView.findViewById(R.id.tvCommercant)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_commande, parent, false)
        return CommandeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommandeViewHolder, position: Int) {
        val commande = commandes[position]
        holder.tvCommercant.text = commande.product?.name ?: "Produit"
        holder.tvDescription.text = "Quantité: ${commande.amount} - Statut: ${commande.delivery_status?.name ?: "?"}"
        holder.itemView.setOnClickListener { onItemClick(commande) }
    }

    override fun getItemCount(): Int = commandes.size
}