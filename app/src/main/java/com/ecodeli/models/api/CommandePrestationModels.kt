package com.ecodeli.models.api

// Modèles pour les commandes
data class CommandeRequest(
    val commercant: String,
    val description: String,
    val montant: Double,
    val adresse_livraison: String,
    val notes: String? = null
)

data class CommandeResponse(
    val _id: Int,
    val client: UserInfo?,
    val commercant: String,
    val description: String,
    val montant: Double,
    val status: String,
    val adresse_livraison: String,
    val date_commande: String,
    val notes: String?
)

// Modèles pour les prestations
data class PrestationRequest(
    val titre: String,
    val description: String,
    val tarif: Double? = 0.0,
    val date_prestation: String,
    val duree_estimee: Int? = 60,
    val adresse: String,
    val notes: String? = null
)

data class PrestationResponse(
    val _id: Int,
    val client: UserInfo?,
    val prestataire: UserInfo?,
    val titre: String,
    val description: String,
    val tarif: Double,
    val status: String,
    val date_prestation: String,
    val duree_estimee: Int,
    val adresse: String,
    val notes: String?,
    val date_creation: String
)

// Réponses de validation
data class ValidationResponse(
    val message: String,
    val commande: CommandeResponse? = null,
    val prestation: PrestationResponse? = null
)