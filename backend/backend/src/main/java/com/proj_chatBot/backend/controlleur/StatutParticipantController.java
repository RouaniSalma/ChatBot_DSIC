package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.StatutParticipant;
import com.proj_chatBot.backend.service.StatutParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/statuts-participant")
public class StatutParticipantController {

    @Autowired
    private StatutParticipantService statutParticipantService;

    @GetMapping
    public List<StatutParticipant> getAllStatutsParticipant() {
        return statutParticipantService.getAllStatutsParticipant();
    }

    @GetMapping("/{id}")
    public Optional<StatutParticipant> getStatutParticipantById(@PathVariable Long id) {
        return statutParticipantService.getStatutParticipantById(id);
    }

    @PostMapping
    public StatutParticipant createStatutParticipant(@RequestBody StatutParticipant statutParticipant) {
        return statutParticipantService.createStatutParticipant(statutParticipant);
    }
}
