package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.DTOs.ParticipantInscriptionDto;
import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.Participant;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.ParticipantRepository;
import com.proj_chatBot.backend.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;
    private static final Logger logger = LoggerFactory.getLogger(ParticipantController.class);
    @Autowired
    private EvenementRepository evenementRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    // Pour les participants (public)

    @PostMapping("/inscription/{evenementId}")
    public ResponseEntity<?> inscrireParticipant(
            @PathVariable Long evenementId,
            @RequestBody ParticipantInscriptionDto inscriptionDto) {
        try {
            // Vérification de capacité
            Evenement evenement = evenementRepository.findById(evenementId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

            long nombreParticipants = participantRepository.countByEvenement(evenement);
            if (evenement.getCapaciteMax() != null && nombreParticipants >= evenement.getCapaciteMax()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacité maximale est atteinte");
            }

            Participant newParticipant = participantService.createParticipant(inscriptionDto, evenementId);
            return ResponseEntity.ok(newParticipant);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de l'inscription",
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
        }
    }

    // Pour les admins/agents
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<Participant>> getParticipantsByEvenement(@PathVariable Long evenementId) {
        List<Participant> participants = participantService.getParticipantsByEvenement(evenementId);
        return ResponseEntity.ok(participants);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping("/{id}")
    public ResponseEntity<Participant> getParticipantById(@PathVariable Long id) {
        Participant participant = participantService.getParticipantById(id);
        return ResponseEntity.ok(participant);
    }



    @GetMapping("/count/{evenementId}")
    public ResponseEntity<Long> countParticipantsByEvenement(@PathVariable Long evenementId) {
        try {
            Evenement evenement = evenementRepository.findById(evenementId)
                    .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

            long count = participantRepository.countByEvenement(evenement);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des participants", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/disponibilite/{evenementId}")
    public ResponseEntity<Map<String, Object>> verifierDisponibilite(@PathVariable Long evenementId) {
        try {
            Evenement evenement = evenementRepository.findById(evenementId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

            long nombreParticipants = participantRepository.countByEvenement(evenement);
            Integer capaciteMax = evenement.getCapaciteMax();
            boolean disponible = capaciteMax == null || nombreParticipants < capaciteMax;

            Map<String, Object> response = new HashMap<>();
            response.put("disponible", disponible);
            response.put("participantsInscrits", nombreParticipants);
            response.put("capaciteMax", capaciteMax);

            if (capaciteMax != null) {
                response.put("placesRestantes", Math.max(0, capaciteMax - nombreParticipants));
            }

            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erreur vérification disponibilité", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur");
        }
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping("/export-csv/{evenementId}")
    public ResponseEntity<byte[]> exportParticipantsToCsv(@PathVariable Long evenementId) {
        try {
            byte[] csvBytes = participantService.exportParticipantsToCsv(evenementId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "participants.csv");

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de l'export CSV", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
