package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.repository.EvenementRepository;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    public List<Evenement> getAllEvenements() {
        return evenementRepository.findAll();
    }

    public Optional<Evenement> getEvenementById(Long id) {
        return evenementRepository.findById(id);
    }

    public Evenement createEvenement(Evenement evenement) {
        return evenementRepository.save(evenement);
    }

    public Evenement updateEvenement(Long id, Evenement evenementDetails) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evenement not found"));

        // Mets à jour les champs nécessaires
        evenement.setTitre(evenementDetails.getTitre());
        evenement.setDescription(evenementDetails.getDescription());
        // ... autres champs

        return evenementRepository.save(evenement);
    }

    public void deleteEvenement(Long id) {
        evenementRepository.deleteById(id);
    }
}
