package com.ecodeli.services

import android.os.Handler
import android.os.Looper
import com.ecodeli.models.*

class ApiService {

    private val handler = Handler(Looper.getMainLooper())

    // Base de données simulée en mémoire
    private val users = mutableListOf<User>()
    private val commandes = mutableListOf<Commande>()
    private val prestations = mutableListOf<Prestation>()

    init {
        // Données de test
        initTestData()
    }

    private fun initTestData() {
        // Utilisateurs de test
        users.add(User("1", "client@test.fr", "123456", "client", "Pierre", "Dupont", "01/01/1990"))
        users.add(User("2", "marie@test.fr", "123456", "client", "Marie", "Martin", "01/01/1985"))
        users.add(User("3", "admin@ecodeli.fr", "admin123", "admin", "Admin", "System", "01/01/1980"))

        // Commandes de test
        commandes.add(Commande("1", "1", "Pharmacie Central", "Médicaments prescription", 25.80, "livree", "123 Rue de la Paix, 75001 Paris"))
        commandes.add(Commande("2", "1", "Supermarché XYZ", "Courses alimentaires", 45.20, "en_livraison", "456 Avenue des Fleurs, 75002 Paris"))
        commandes.add(Commande("3", "1", "Boulangerie Paul", "Pain et viennoiseries", 12.40, "en_attente", "789 Boulevard Saint-Germain, 75006 Paris"))
        commandes.add(Commande("4", "1", "Fleuriste Roses", "Bouquet de roses", 35.00, "livree", "321 Rue du Commerce, 75015 Paris"))
        commandes.add(Commande("5", "1", "Librairie Mots", "Livres commandés", 28.50, "annulee", "654 Place de la République, 75011 Paris"))

        // Prestations de test
        prestations.add(Prestation("1", "prestataire_1", "1", "Transport médecin", "Accompagnement chez le médecin", 25.0, "terminee", System.currentTimeMillis() - 86400000, 120, "Cabinet médical, 45 rue de la Santé"))
        prestations.add(Prestation("2", "prestataire_2", "1", "Courses alimentaires", "Faire les courses au supermarché", 15.0, "en_cours", System.currentTimeMillis(), 90, "Supermarché Leclerc"))
        prestations.add(Prestation("3", "prestataire_1", "1", "Garde d'animaux", "Garde de chat pendant 2h", 20.0, "demandee", System.currentTimeMillis() + 86400000, 120, "Domicile client"))
    }

    // ==================== AUTHENTIFICATION ====================

    fun login(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        handler.postDelayed({
            val user = users.find { it.email == email && it.password == password }

            if (user != null) {
                callback(true, "client", "Connexion réussie")
            } else {
                callback(false, null, "Email ou mot de passe incorrect")
            }
        }, 1500)
    }

    fun register(userData: Map<String, String>, callback: (Boolean, String?) -> Unit) {
        handler.postDelayed({
            val email = userData["email"] ?: ""

            val existingUser = users.find { it.email == email }

            if (existingUser != null) {
                callback(false, "Un compte avec cet email existe déjà")
                return@postDelayed
            }

            val newUser = User(
                id = (users.size + 1).toString(),
                email = email,
                password = userData["password"] ?: "",
                type = "client",
                nom = userData["nom"] ?: "",
                prenom = userData["prenom"] ?: "",
                birthDate = userData["birthDate"] ?: ""
            )

            users.add(newUser)
            callback(true, "Inscription réussie")
        }, 2000)
    }

    // ==================== COMMANDES CLIENT ====================

    fun getClientCommandes(clientId: String, callback: (List<Commande>) -> Unit) {
        handler.postDelayed({
            val clientCommandes = commandes.filter { it.clientId == clientId }
                .sortedByDescending { it.dateCommande }
            callback(clientCommandes)
        }, 1000)
    }

    fun createCommande(commande: Commande, callback: (Boolean, String?) -> Unit) {
        handler.postDelayed({
            val newCommande = commande.copy(
                id = (commandes.size + 1).toString(),
                dateCommande = System.currentTimeMillis()
            )
            commandes.add(newCommande)
            callback(true, "Commande créée avec succès")
        }, 1500)
    }

    fun validateLivraison(commandeId: String, codeValidation: String, callback: (Boolean, String?) -> Unit) {
        handler.postDelayed({
            val commandeIndex = commandes.indexOfFirst { it.id == commandeId }

            if (commandeIndex != -1) {
                val commande = commandes[commandeIndex]
                if (commande.status == "livree") {
                    // Valider avec n'importe quel code pour la démo
                    commandes[commandeIndex] = commande.copy(status = "validee")
                    callback(true, "Livraison validée avec succès")
                } else {
                    callback(false, "Cette commande ne peut pas être validée")
                }
            } else {
                callback(false, "Commande non trouvée")
            }
        }, 800)
    }

    // ==================== PRESTATIONS ====================

    fun getClientPrestations(clientId: String, callback: (List<Prestation>) -> Unit) {
        handler.postDelayed({
            val clientPrestations = prestations.filter { it.clientId == clientId }
                .sortedByDescending { it.datePrestation }
            callback(clientPrestations)
        }, 1000)
    }

    fun createPrestation(prestation: Prestation, callback: (Boolean, String?) -> Unit) {
        handler.postDelayed({
            val newPrestation = prestation.copy(
                id = (prestations.size + 1).toString()
            )
            prestations.add(newPrestation)
            callback(true, "Prestation créée avec succès")
        }, 1500)
    }

    // ==================== STATISTIQUES ====================

    fun getClientStats(clientId: String, callback: (Map<String, Any>) -> Unit) {
        handler.postDelayed({
            val clientCommandes = commandes.filter { it.clientId == clientId }
            val clientPrestations = prestations.filter { it.clientId == clientId }

            val totalCommandes = clientCommandes.size
            val totalPrestations = clientPrestations.size
            val totalDepense = clientCommandes.sumOf { it.montant } + clientPrestations.sumOf { it.tarif }
            val commandesLivrees = clientCommandes.count { it.status == "livree" }

            val stats = mapOf(
                "total_commandes" to totalCommandes,
                "total_prestations" to totalPrestations,
                "total_depense" to String.format("%.2f", totalDepense),
                "commandes_livrees" to commandesLivrees,
                "taux_satisfaction" to 4.2,
                "economie_carbone" to "${totalCommandes * 2.5}kg CO2"
            )

            callback(stats)
        }, 800)
    }

    // Méthode à ajouter dans ApiService.kt dans la section PRESTATIONS

    fun cancelPrestation(prestationId: String, callback: (Boolean, String?) -> Unit) {
        handler.postDelayed({
            val prestationIndex = prestations.indexOfFirst { it.id == prestationId }

            if (prestationIndex != -1) {
                val prestation = prestations[prestationIndex]
                prestations[prestationIndex] = prestation.copy(status = "annulee")
                callback(true, "Prestation annulée")
            } else {
                callback(false, "Prestation non trouvée")
            }
        }, 800)
    }
    

}