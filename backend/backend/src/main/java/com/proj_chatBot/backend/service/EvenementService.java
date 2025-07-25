package com.proj_chatBot.backend.service;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.enums.StatutEvenement;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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
    public Evenement createEvenement(@RequestBody @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")Evenement evenement, Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        evenement.setUtilisateur(utilisateur);

        evenement.setDateCreation(new Date());

        // Associer le type existant
        TypeEvenement type = evenement.getType();
        if (type.getIdType() != null) {
            type = typeEvenementRepository.findById(type.getIdType())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
            evenement.setType(type);
        } else {
            throw new RuntimeException("Type d'événement obligatoire !");
        }

        // Calculer le statut selon les dates de l'événement
        evenement.setStatut(calculerStatut(evenement));

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

        // Mettre à jour les dates et le lieu
        evenement.setDateDebut(evenementDetails.getDateDebut());
        evenement.setDateFin(evenementDetails.getDateFin());
        evenement.setLieu(evenementDetails.getLieu());

        // Recalculer le statut
        evenement.setStatut(calculerStatut(evenement));

        return evenementRepository.save(evenement);
    }

    // Méthode utilitaire pour calculer le statut
    public StatutEvenement calculerStatut(Evenement evenement) {
        LocalDateTime now = LocalDateTime.now();
        if (evenement.getDateFin().isBefore(now)) {
            return StatutEvenement.TERMINE;
        } else if (evenement.getDateDebut().isAfter(now)) {
            return StatutEvenement.PROCHAIN;
        } else {
            return StatutEvenement.EN_COURS;
        }
    }

    // Récupérer tous les événements
    public List<Evenement> getAllEvenements() {
        List<Evenement> evenements = evenementRepository.findAll();
        for (Evenement ev : evenements) {
            ev.setStatut(calculerStatut(ev));
        }
        return evenements;
    }

    // Récupérer un événement par son ID
    public Evenement getEvenementById(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
    }

    // Supprimer un événement par son ID
    public void deleteEvenement(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new RuntimeException("Événement non trouvé");
        }
        evenementRepository.deleteById(id);
    }
    // Dans EvenementService.java

    // Récupérer les événements paginés et filtrés
    public Page<Evenement> getEvenementsFiltresEtPages(Optional<Long> typeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (typeId.isPresent()) {
            TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
            return evenementRepository.findByType(type, pageable);
        } else {
            return evenementRepository.findAll(pageable);
        }
    }

    // Compter les événements par type
    public long countEvenementsByType(Optional<Long> typeId) {
        if (typeId.isPresent()) {
            TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
            return evenementRepository.countByType(type);
        } else {
            return evenementRepository.count();
        }
    }
}
