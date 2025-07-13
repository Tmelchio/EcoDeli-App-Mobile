package com.ecodeli.models

// ==================== UTILISATEUR ====================
data class User(
    val id: String,
    val email: String,
    val password: String,
    val type: String, // "client", "livreur", "commercant", "prestataire"
    val nom: String,
    val prenom: String,
    val birthDate: String,
    val phone: String = "",
    val address: String = "",
    val profilePicture: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== COMMANDE ====================
data class Commande(
    val id: String,
    val clientId: String,
    val commercant: String,
    val description: String,
    val montant: Double,
    val status: String, // "en_attente", "en_livraison", "livree", "annulee"
    val adresseLivraison: String = "",
    val dateCommande: Long = System.currentTimeMillis(),
    val dateLivraisonSouhaitee: Long = 0,
    val notes: String = ""
)

// ==================== LIVRAISON ====================
data class Livraison(
    val id: String,
    val livreurId: String,
    val commandeId: String,
    val adresseDepart: String,
    val adresseArrivee: String,
    val status: String, // "en_cours", "terminee", "annulee"
    val dateDebut: Long = System.currentTimeMillis(),
    val dateFin: Long = 0,
    val distance: Double = 0.0,
    val tempEstime: Int = 30, // minutes
    val tarif: Double = 5.0
)

// ==================== TRAJET LIVREUR ====================
data class Trajet(
    val id: String,
    val livreurId: String,
    val villeDepart: String,
    val villeArrivee: String,
    val dateTrajet: String, // Format: "dd/MM/yyyy"
    val heureDepart: String, // Format: "HH:mm"
    val status: String, // "planifie", "en_cours", "termine", "annule"
    val capacite: Int = 1, // Nombre de colis max
    val vehicule: String = "voiture", // "velo", "scooter", "voiture", "pied"
    val notes: String = "",
    val dateCreation: Long = System.currentTimeMillis()
)

// ==================== ANNONCE ====================
data class Annonce(
    val id: String,
    val userId: String,
    val userType: String, // "client", "livreur"
    val titre: String,
    val description: String,
    val type: String, // "livraison", "service", "achat"
    val prix: Double,
    val localisation: String,
    val dateCreation: Long = System.currentTimeMillis(),
    val dateExpiration: Long = 0,
    val isActive: Boolean = true,
    val tags: List<String> = emptyList()
)

// ==================== PRESTATION ====================
data class Prestation(
    val id: String,
    val prestataireId: String,
    val clientId: String,
    val titre: String,
    val description: String,
    val tarif: Double,
    val status: String, // "demandee", "acceptee", "en_cours", "terminee", "annulee"
    val datePrestation: Long,
    val dureeEstimee: Int, // minutes
    val adresse: String,
    val notes: String = ""
)

// ==================== EVALUATION ====================
data class Evaluation(
    val id: String,
    val evaluateurId: String,
    val evalueId: String,
    val type: String, // "livraison", "prestation", "service"
    val referenceId: String, // ID de la commande/livraison/prestation
    val note: Int, // 1-5
    val commentaire: String,
    val dateEvaluation: Long = System.currentTimeMillis()
)

// ==================== NOTIFICATION ====================
data class Notification(
    val id: String,
    val userId: String,
    val titre: String,
    val message: String,
    val type: String, // "info", "commande", "livraison", "evaluation"
    val isRead: Boolean = false,
    val dateCreation: Long = System.currentTimeMillis(),
    val actionUrl: String = ""
)

// ==================== PLANNING ====================
data class PlanningSlot(
    val id: String,
    val userId: String,
    val dateDebut: Long,
    val dateFin: Long,
    val isAvailable: Boolean = true,
    val type: String, // "livraison", "prestation"
    val notes: String = ""
)

// ==================== PAIEMENT ====================
data class Paiement(
    val id: String,
    val payeurId: String,
    val beneficiaireId: String,
    val montant: Double,
    val type: String, // "commande", "livraison", "prestation"
    val referenceId: String, // ID de la commande/livraison/prestation
    val status: String, // "en_attente", "valide", "refuse", "rembourse"
    val methodePaiement: String, // "carte", "paypal", "virement", "especes"
    val datePaiement: Long = System.currentTimeMillis(),
    val fraisService: Double = 0.0
)

// ==================== CONVERSATION ====================
data class Conversation(
    val id: String,
    val participants: List<String>, // IDs des utilisateurs
    val dernierMessage: String,
    val dateLastMessage: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

data class Message(
    val id: String,
    val conversationId: String,
    val expediteurId: String,
    val contenu: String,
    val type: String = "text", // "text", "image", "file"
    val dateEnvoi: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// ==================== LOCALISATION ====================
data class Position(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== VEHICULE (LIVREUR) ====================
data class Vehicule(
    val id: String,
    val livreurId: String,
    val type: String, // "velo", "scooter", "voiture", "pied"
    val marque: String = "",
    val modele: String = "",
    val immatriculation: String = "",
    val capaciteMax: Double = 0.0, // kg
    val isActive: Boolean = true
)

// ==================== PRODUIT/SERVICE ====================
data class Produit(
    val id: String,
    val commercantId: String,
    val nom: String,
    val description: String,
    val prix: Double,
    val categorie: String,
    val isAvailable: Boolean = true,
    val stock: Int = 0,
    val images: List<String> = emptyList()
)

// ==================== ZONE DE LIVRAISON ====================
data class ZoneLivraison(
    val id: String,
    val livreurId: String,
    val nom: String,
    val coordonnees: List<Position>, // Polygone de la zone
    val isActive: Boolean = true,
    val tarifBase: Double = 5.0
)

// ==================== STATISTIQUES ====================
data class UserStats(
    val userId: String,
    val type: String,
    val totalActivites: Int = 0,
    val noteMoyenne: Double = 0.0,
    val totalGains: Double = 0.0,
    val totalDepenses: Double = 0.0,
    val activitesTerminees: Int = 0,
    val tauxReussite: Double = 0.0,
    val derniereMiseAJour: Long = System.currentTimeMillis()
)