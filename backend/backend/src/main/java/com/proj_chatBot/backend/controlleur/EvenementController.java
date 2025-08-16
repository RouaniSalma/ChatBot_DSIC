package com.proj_chatBot.backend.controlleur;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import com.proj_chatBot.backend.service.EvenementService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.util.function.SupplierUtils.resolve;

@Slf4j
@RestController
@RequestMapping("/api/evenements")
public class EvenementController {
    private final String uploadDir;

    @Autowired
    private EvenementService evenementService;
    @Autowired
    private TypeEvenementRepository typeEvenementRepository;
    @Autowired
    private EvenementRepository evenementRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    public EvenementController(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvenement(
            @RequestPart("evenement") String evenementStr,
            Authentication authentication,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
        // 1️⃣ Récupérer l'utilisateur connecté depuis le JWT
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2️⃣ Désérialiser l'événement
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Evenement evenement = mapper.readValue(evenementStr, Evenement.class);
        evenement.setUtilisateur(utilisateur);


            mapper.registerModule(new JavaTimeModule());


            // Gestion de l'image
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir).resolve(imageName);
                Files.createDirectories(imagePath.getParent());
                Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
                evenement.setImagePath(imageName);
            }

            Evenement savedEvent = evenementService.createEvenement(evenement);
            return ResponseEntity.ok(savedEvent);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Format JSON invalide");
        } catch (IOException e) {
            log.error("Erreur de traitement de fichier", e);
            return ResponseEntity.internalServerError().body("Erreur de traitement de fichier");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Erreur inattendue", e);
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateEvenement(
            @PathVariable Long id,
            @RequestPart("evenement") String evenementStr,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Evenement evenementDetails = mapper.readValue(evenementStr, Evenement.class);

            // Récupérer l'événement existant
            Evenement existingEvent = evenementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

            // Supprimer l'ancienne image si elle existe ET si une nouvelle image est fournie
            if (imageFile != null && !imageFile.isEmpty() && existingEvent.getImagePath() != null) {
                Path oldImagePath = Paths.get(uploadDir).resolve(existingEvent.getImagePath());
                Files.deleteIfExists(oldImagePath);
            }

            // Gestion de la nouvelle image
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir).resolve(imageName);
                Files.createDirectories(imagePath.getParent());
                Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
                evenementDetails.setImagePath(imageName);
            } else {
                // Conserver l'ancienne image si aucune nouvelle n'est fournie
                evenementDetails.setImagePath(existingEvent.getImagePath());
            }

            Evenement updatedEvent = evenementService.updateEvenement(id, evenementDetails);
            return ResponseEntity.ok(updatedEvent);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Format JSON invalide");
        } catch (IOException e) {
            log.error("Erreur de traitement de fichier", e);
            return ResponseEntity.internalServerError().body("Erreur de traitement de fichier");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Erreur inattendue", e);
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }




    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AGENT_WILAYA')")
    @GetMapping
    public List<Evenement> getAllEvenements(Authentication authentication) {
        System.out.println("Accès autorisé au endpoint getAllEvenements");

        // Modification ici - utilisation de l'email directement
        String email = authentication.getName();
        Utilisateur utilisateurConnecte = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return evenementService.getAllEvenements(utilisateurConnecte);
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
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            Authentication authentication) {

        try {
            // Récupération de l'utilisateur connecté par email
            String email = authentication.getName();
            Utilisateur utilisateurConnecte = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Page<Evenement> pageEvenements = evenementService.getEvenementsFiltresEtPages(
                    Optional.ofNullable(typeId),
                    page,
                    size,
                    utilisateurConnecte
            );

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

        } catch (Exception e) {
            log.error("Erreur lors du filtrage des événements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Erreur interne du serveur"));
        }
    }

}
