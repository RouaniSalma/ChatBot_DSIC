package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Participant;
import com.proj_chatBot.backend.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    // Pour les participants (public)
    @PostMapping("/inscription/{evenementId}")
    public ResponseEntity<Participant> inscrireParticipant(
            @PathVariable Long evenementId,
            @RequestParam Long statutId,
            @RequestBody Participant participant) {

        Participant newParticipant = participantService.createParticipant(participant, evenementId, statutId);
        return ResponseEntity.ok(newParticipant);
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/{evenementId}")
    public ResponseEntity<Long> countParticipantsByEvenement(@PathVariable Long evenementId) {
        long count = participantService.countParticipantsByEvenement(evenementId);
        return ResponseEntity.ok(count);
    }
}
