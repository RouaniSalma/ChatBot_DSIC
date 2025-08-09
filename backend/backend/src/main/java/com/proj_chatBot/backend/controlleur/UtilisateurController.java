package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.DTOs.CreateUtilisateurDTO;
import com.proj_chatBot.backend.DTOs.UtilisateurDTO;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import com.proj_chatBot.backend.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

// UtilisateurController.java
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
    }


    @GetMapping
    public ResponseEntity<Page<UtilisateurDTO>> getAllUtilisateurs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(utilisateurService.findAllPaginated(page, size));
    }
    @PostMapping
    public ResponseEntity<UtilisateurDTO> createUtilisateur(@RequestBody CreateUtilisateurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(utilisateurService.createUtilisateur(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> updateUtilisateur(
            @PathVariable Long id,
            @RequestBody CreateUtilisateurDTO dto) {
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/par-service/{idService}")
    public ResponseEntity<List<Utilisateur>> getUtilisateursParService(@PathVariable Long idService) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findByService_IdService(idService);
        return ResponseEntity.ok(utilisateurs);
    }

}
