package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.ServiceResponse

class PrestationAdapterReal(
    private val prestations: List<ServiceResponse>,
    private val onItemClick: (ServiceResponse) -> Unit
) : RecyclerView.Adapter<PrestationAdapterReal.PrestationViewHolder>() {

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
        holder.tvTitre.text = prestation.name
        holder.tvDescription.text = prestation.description
        holder.tvTarif.text = "${prestation.price}€"
        holder.tvDate.text = prestation.date
        holder.itemView.setOnClickListener { onItemClick(prestation) }
    }

    override fun getItemCount(): Int = prestations.size
}