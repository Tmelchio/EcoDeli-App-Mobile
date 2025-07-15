package com.ecodeli.models.api

// ==================== PRODUITS ====================
// Pour créer un nouveau produit (quand on est vendeur)
data class ProductRequest(
    val name: String,        // Nom du produit
    val price: Double,       // Prix
    val size: Int,          // ID de la taille (1=S, 2=M, 3=L, etc.)
    val location: LocationData  // Données de localisation
)

// Réponse quand on récupère un produit - CORRIGÉ
data class ProductResponse(
    val _id: Int,
    val name: String,
    val image: String?,
    val price: Double,
    val size: Any, // Peut être un objet PackageSize ou un simple ID
    val seller: Any, // Peut être un objet UserInfo ou un simple ID
    val location: Any? // Peut être un objet LocationInfo ou un simple ID
)

// ==================== DEMANDES DE PRODUITS (Remplace les "commandes") ====================
// Pour commander un produit existant
data class ProductRequestRequest(
    val product: Int,    // ID du produit qu'on veut acheter
    val amount: Int,     // Quantité
    val location: Int    // ID de notre adresse de livraison
)

// Réponse = notre "commande" - CORRIGÉ
data class ProductRequestResponse(
    val _id: Int,
    val creation_date: String,
    val date: String?,
    val accepted_date: String?,
    val validation_code: String?,
    val delivery_location: Any?, // Peut être un objet LocationInfo ou null
    val receiver: Any?, // Peut être un objet UserInfo ou un simple ID
    val product: Any?, // Peut être un objet ProductResponse ou null
    val amount: Int,
    val delivery: Any?, // Peut être un objet DeliveryInfo ou null
    val delivery_status: Any? // Peut être un objet DeliveryStatusInfo ou null
)

// ==================== SERVICES (Remplace les "prestations") ====================
// Pour demander un service
data class ServiceRequest(
    val name: String,        // Type de service
    val description: String, // Description détaillée
    val price: Double,       // Prix proposé
    val date: String        // Date souhaitée (format ISO)
)

// Réponse = notre demande de service - CORRIGÉ
data class ServiceResponse(
    val _id: Int,
    val creation_date: String,
    val date: String,           // Date du service
    val name: String,           // Type de service
    val image: String?,
    val description: String,
    val price: Double,
    val user: Any?, // Peut être un objet UserInfo ou un simple ID
    val actor: Any? // Peut être un objet UserInfo ou null
)

// ==================== MODÈLES AUXILIAIRES ====================
data class PackageSize(
    val _id: Int,
    val name: String,    // "S", "M", "L", "XL", "XXL"
    val size: Int       // 1, 2, 3, 4, 5
)

data class LocationInfo(
    val _id: Int,
    val user: Int,
    val city: String,
    val zipcode: String,
    val address: String
)

data class DeliveryInfo(
    val _id: Int,
    val deliveryman: Any?, // Peut être un objet UserInfo ou null
    val latitude: Double?,
    val longitude: Double?
)

data class DeliveryStatusInfo(
    val _id: Int,
    val name: String    // "pending", "accepted", "in_progress", "delivered", "cancelled"
)

// ==================== LOCATIONS ====================
data class LocationRequest(
    val location: LocationData
)

data class LocationData(
    val city: String,
    val zipcode: String,
    val address: String
)

// ==================== MODÈLES DE COMPATIBILITÉ ====================
// Pour garder la compatibilité avec l'ancien code

data class CommandeRequest(
    val commercant: String,
    val description: String,
    val montant: Double,
    val adresse_livraison: String,
    val notes: String? = null
)

data class PrestationRequest(
    val titre: String,
    val description: String,
    val tarif: Double? = 0.0,
    val date_prestation: String,
    val duree_estimee: Int? = 60,
    val adresse: String,
    val notes: String? = null
)

// ==================== ANCIENS MODÈLES (pour compatibilité) ====================
data class PrestationResponse(
    val _id: Int,
    val titre: String,
    val description: String,
    val tarif: Double,
    val status: String,
    val adresse: String,
    val duree_estimee: Int,
    val date_prestation: String
)