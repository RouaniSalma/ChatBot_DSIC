package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @PostMapping
    public Evenement createEvenement(@RequestBody Evenement evenement, @RequestParam Long utilisateurId) {
        return evenementService.createEvenement(evenement, utilisateurId);
    }

    @PutMapping("/{id}")
    public Evenement updateEvenement(@PathVariable Long id, @RequestBody Evenement evenementDetails) {
        return evenementService.updateEvenement(id, evenementDetails);
    }





}