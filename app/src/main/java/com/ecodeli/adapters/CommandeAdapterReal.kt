package com.ecodeli.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecodeli.R
import com.ecodeli.models.api.ProductRequestResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.util.Log

class CommandeAdapterReal(
    private val commandes: List<ProductRequestResponse>,
    private val onItemClick: (ProductRequestResponse) -> Unit
) : RecyclerView.Adapter<CommandeAdapterReal.CommandeViewHolder>() {

    private val gson = Gson()

    companion object {
        private const val TAG = "CommandeAdapterReal"
    }

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

        // Extraire le nom du produit de manière sécurisée
        val productName = extractProductName(commande.product)

        // Extraire le statut de manière sécurisée
        val statusName = extractStatusName(commande.delivery_status)

        // Extraire le prix du produit
        val productPrice = extractProductPrice(commande.product)

        holder.tvCommercant.text = productName
        holder.tvDescription.text = "Quantité: ${commande.amount} - Prix: ${productPrice}€ - Statut: $statusName"

        holder.itemView.setOnClickListener { onItemClick(commande) }
    }

    private fun extractProductName(productField: Any?): String {
        return try {
            when (productField) {
                is JsonObject -> {
                    val productObj = productField as JsonObject
                    productObj.get("name")?.asString ?: "Produit"
                }
                is Map<*, *> -> {
                    val productMap = productField as Map<*, *>
                    productMap["name"] as? String ?: "Produit"
                }
                else -> {
                    Log.d(TAG, "Product field type: ${productField?.javaClass}")
                    "Produit"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction nom produit", e)
            "Produit"
        }
    }

    private fun extractProductPrice(productField: Any?): String {
        return try {
            when (productField) {
                is JsonObject -> {
                    val productObj = productField as JsonObject
                    val price = productObj.get("price")?.asDouble ?: 0.0
                    String.format("%.2f", price)
                }
                is Map<*, *> -> {
                    val productMap = productField as Map<*, *>
                    val price = (productMap["price"] as? Number)?.toDouble() ?: 0.0
                    String.format("%.2f", price)
                }
                else -> {
                    Log.d(TAG, "Product field type for price: ${productField?.javaClass}")
                    "0.00"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction prix produit", e)
            "0.00"
        }
    }

    private fun extractStatusName(statusField: Any?): String {
        return try {
            when (statusField) {
                is JsonObject -> {
                    val statusObj = statusField as JsonObject
                    val statusName = statusObj.get("name")?.asString ?: "inconnu"
                    translateStatus(statusName)
                }
                is Map<*, *> -> {
                    val statusMap = statusField as Map<*, *>
                    val statusName = statusMap["name"] as? String ?: "inconnu"
                    translateStatus(statusName)
                }
                else -> {
                    Log.d(TAG, "Status field type: ${statusField?.javaClass}")
                    "inconnu"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur extraction statut", e)
            "inconnu"
        }
    }

    private fun translateStatus(status: String): String {
        return when (status) {
            "pending" -> "En attente"
            "accepted" -> "Acceptée"
            "in_progress" -> "En cours"
            "delivered" -> "Livrée"
            "cancelled" -> "Annulée"
            else -> status
        }
    }

    override fun getItemCount(): Int = commandes.size
}