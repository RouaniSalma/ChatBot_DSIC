package com.proj_chatBot.backend.service;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.StatutParticipant;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.StatutParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.Participant;
import com.proj_chatBot.backend.repository.ParticipantRepository;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private StatutParticipantRepository statutParticipantRepository;

    // Créer un participant
    public Participant createParticipant(Participant participant, Long evenementId, Long statutId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        StatutParticipant statut = statutParticipantRepository.findById(statutId)
                .orElseThrow(() -> new RuntimeException("Statut participant non trouvé"));

        participant.setEvenement(evenement);
        participant.setStatut(statut);

        return participantRepository.save(participant);
    }

    // Récupérer tous les participants d'un événement
    public List<Participant> getParticipantsByEvenement(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        return participantRepository.findByEvenement(evenement);
    }

    // Récupérer un participant par son ID
    public Participant getParticipantById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));
    }

    // Supprimer un participant
    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }

    // Compter les participants par événement
    public long countParticipantsByEvenement(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        return participantRepository.countByEvenement(evenement);
    }
}
