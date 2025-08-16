package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/evenements")
public class PublicEvenementController {

    @Autowired
    private EvenementService evenementService;

    @GetMapping
    public ResponseEntity<List<Evenement>> getEvenementsPublic() {
        LocalDateTime now = LocalDateTime.now();

        List<Evenement> sorted = evenementService.getEvenementsPublic().stream()
                .sorted(Comparator.comparing((Evenement e) -> {
                    if (e.getDateDebut().isAfter(now)) return 1; // À venir en premier
                    if (e.getDateDebut().isBefore(now) && e.getDateFin().isAfter(now)) return 2; // En cours ensuite
                    return 3; // Terminés à la fin
                }).thenComparing(Evenement::getDateDebut))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sorted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evenement> getEvenementPublicById(@PathVariable Long id) {
        Evenement evenement = evenementService.getEvenementById(id);
        return ResponseEntity.ok(evenement);
    }
}