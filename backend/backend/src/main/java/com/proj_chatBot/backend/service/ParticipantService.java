package com.proj_chatBot.backend.service;

import com.proj_chatBot.backend.DTOs.ParticipantInscriptionDto;
import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.StatutParticipant;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.StatutParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.Participant;
import com.proj_chatBot.backend.repository.ParticipantRepository;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private StatutParticipantRepository statutParticipantRepository;

    // Créer un participant
    public Participant createParticipant(ParticipantInscriptionDto dto, Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        // Vérification de la capacité
        long nombreParticipants = countParticipantsByEvenement(evenementId);
        if (evenement.getCapaciteMax() != null && nombreParticipants >= evenement.getCapaciteMax()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La capacité maximale de cet événement est atteinte. Impossible de s'inscrire."
            );
        }

        StatutParticipant statut = statutParticipantRepository.findById(dto.getStatutId())
                .orElseThrow(() -> new RuntimeException("Statut participant non trouvé"));

        // Vérification supplémentaire de la signature
        if (dto.getSignature() == null || dto.getSignature().isBlank()) {
            throw new RuntimeException("La signature est requise");
        }

        Participant participant = new Participant();
        participant.setNom(dto.getNom());
        participant.setPrenom(dto.getPrenom());
        participant.setEmail(dto.getEmail());
        participant.setTelephone(dto.getTelephone());
        participant.setSignature(dto.getSignature());
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




    // Compter les participants par événement
    public long countParticipantsByEvenement(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        return participantRepository.countByEvenement(evenement);
    }
    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }

    public byte[] exportParticipantsToCsv(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        List<Participant> participants = participantRepository.findByEvenement(evenement);

        StringBuilder csvBuilder = new StringBuilder();
        // BOM pour UTF-8
        csvBuilder.append("\uFEFF");
        // En-tête avec accents
        csvBuilder.append("ID,Nom,Prénom,Email,Téléphone,Statut,Signature\n");

        // Données
        for (Participant p : participants) {
            // Formater le téléphone pour ajouter le 0 initial si nécessaire
            String telephone = p.getTelephone();
            if (telephone != null && !telephone.isEmpty() && !telephone.startsWith("0")) {
                telephone = "0" + telephone;
            }

            csvBuilder.append(p.getIdParticipant()).append(",")
                    .append(escapeCsv(p.getNom())).append(",")
                    .append(escapeCsv(p.getPrenom())).append(",")
                    .append(escapeCsv(p.getEmail())).append(",")
                    .append(escapeCsv(telephone)).append(",") // Utiliser le téléphone formaté
                    .append(p.getStatut() != null ? escapeCsv(p.getStatut().getLibelle()) : "").append(",")
                    .append(escapeCsv(p.getSignature() != null ? "Oui" : "Non"))
                    .append("\n");
        }

        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Échapper les guillemets et encapsuler dans des guillemets si contient des virgules ou sauts de ligne
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
