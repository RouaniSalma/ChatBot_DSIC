package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @PostMapping
    public Evenement createEvenement(@RequestBody Evenement evenement, @RequestParam Long utilisateurId) {
        System.out.println("Accès autorisé au endpoint createEvenement");
        return evenementService.createEvenement(evenement, utilisateurId);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @PutMapping("/{id}")
    public Evenement updateEvenement(@PathVariable Long id, @RequestBody Evenement evenementDetails) {
        return evenementService.updateEvenement(id, evenementDetails);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping
    public List<Evenement> getAllEvenements() {
        System.out.println("Accès autorisé au endpoint getAllEvenements");
        return evenementService.getAllEvenements();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping("/{id}")
    public Evenement getEvenementById(@PathVariable Long id) {
        return evenementService.getEvenementById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @DeleteMapping("/{id}")
    public void deleteEvenement(@PathVariable Long id) {
        System.out.println("Suppression demandée pour l'événement " + id);
        evenementService.deleteEvenement(id);
    }
}