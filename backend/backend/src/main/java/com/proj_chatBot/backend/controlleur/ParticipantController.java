package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Participant;
import com.proj_chatBot.backend.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    @GetMapping
    public List<Participant> getAllParticipants() {
        return participantService.getAllParticipants();
    }

    @GetMapping("/{id}")
    public Optional<Participant> getParticipantById(@PathVariable Long id) {
        return participantService.getParticipantById(id);
    }



    @DeleteMapping("/{id}")
    public void deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
    }

    @PostMapping
    public Participant createParticipant(@RequestBody Participant participant) {
        Participant saved = participantService.createParticipant(participant);
        // Recharge le participant pour avoir les relations complètes
        return participantService.getParticipantById(saved.getIdParticipant()).get();
    }
}
