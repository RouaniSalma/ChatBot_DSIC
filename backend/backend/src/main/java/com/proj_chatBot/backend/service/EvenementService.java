package com.proj_chatBot.backend.service;


import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.enums.StatutEvenement;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private TypeEvenementRepository typeEvenementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // Création d'un événement
    public Evenement createEvenement(Evenement evenement, Long utilisateurId) {
        // Associer l'utilisateur créateur
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        evenement.setUtilisateur(utilisateur);

        // Date de création
        evenement.setDateCreation(new Date());

        // Gérer le type d'événement (créer ou lier à un existant)
        TypeEvenement type = evenement.getType();
        if (type.getIdType() != null) {
            type = typeEvenementRepository.findById(type.getIdType())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
        } else {
            type = typeEvenementRepository.save(type);
        }
        evenement.setType(type);

        // Calculer le statut
        evenement.setStatut(calculerStatut(type.getDateDebut(), type.getDateFin()));

        return evenementRepository.save(evenement);
    }

    // Mise à jour d'un événement
    public Evenement updateEvenement(Long id, Evenement evenementDetails) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        evenement.setTitre(evenementDetails.getTitre());
        evenement.setDescription(evenementDetails.getDescription());
        evenement.setCapaciteMax(evenementDetails.getCapaciteMax());

        // Gérer le type d'événement
        TypeEvenement type = evenementDetails.getType();
        if (type.getIdType() != null) {
            type = typeEvenementRepository.findById(type.getIdType())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
        } else {
            type = typeEvenementRepository.save(type);
        }
        evenement.setType(type);

        // Recalculer le statut
        evenement.setStatut(calculerStatut(type.getDateDebut(), type.getDateFin()));

        return evenementRepository.save(evenement);
    }

    // Méthode utilitaire pour calculer le statut
    private StatutEvenement calculerStatut(LocalDateTime dateDebut, LocalDateTime dateFin) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dateDebut)) {
            return StatutEvenement.PROCHAIN;
        } else if (now.isAfter(dateFin)) {
            return StatutEvenement.TERMINE;
        } else {
            return StatutEvenement.EN_COURS;
        }
    }
}
