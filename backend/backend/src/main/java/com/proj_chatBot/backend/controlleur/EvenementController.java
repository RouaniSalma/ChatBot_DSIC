package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;
import com.proj_chatBot.backend.service.EvenementService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;
    @Autowired
    private TypeEvenementRepository typeEvenementRepository;
    @Autowired
    private EvenementRepository evenementRepository;


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @PostMapping
    public Evenement createEvenement(@RequestBody Evenement evenement, @RequestParam Long utilisateurId) {
        System.out.println("Accès autorisé au endpoint createEvenement");
        System.out.println("\n=== AVANT TRAITEMENT ===");
        System.out.println("Reçu du frontend - dateDebut: " + evenement.getDateDebut());
        System.out.println("Reçu du frontend - dateFin: " + evenement.getDateFin());
        System.out.println("Type des dates: " + evenement.getDateDebut().getClass().getName());
        Evenement result = evenementService.createEvenement(evenement, utilisateurId);
        // Log après traitement
        System.out.println("\n=== APRÈS TRAITEMENT ===");
        System.out.println("Retourné par le service - dateDebut: " + result.getDateDebut());
        System.out.println("Retourné par le service - dateFin: " + result.getDateFin());
        System.out.println("Type des dates retournées: " + result.getDateDebut().getClass().getName());

        return result;
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
    // Dans EvenementController.java

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getEvenementsFiltres(
            @RequestParam(required = false) Long typeId, // Reste en Long
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        log.info("TypeID reçu: {}", typeId);

        try {
            Page<Evenement> pageEvenements;
            Pageable pageable = PageRequest.of(page, size);

            if (typeId != null) {
                TypeEvenement type = typeEvenementRepository.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("Type non trouvé"));
                pageEvenements = evenementRepository.findByType(type, pageable);
            } else {
                pageEvenements = evenementRepository.findAll(pageable);
            }

            // Mise à jour des statuts
            pageEvenements.getContent().forEach(ev -> {
                ev.setStatut(evenementService.calculerStatut(ev));
            });

            Map<String, Object> response = new HashMap<>();
            response.put("evenements", pageEvenements.getContent());
            response.put("currentPage", pageEvenements.getNumber());
            response.put("totalItems", pageEvenements.getTotalElements());
            response.put("totalPages", pageEvenements.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            throw e; // Re-lance les exceptions de réponse déjà gérées
        } catch (Exception e) {
            log.error("Erreur lors du filtrage des événements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Erreur interne du serveur"));
        }
    }
}
