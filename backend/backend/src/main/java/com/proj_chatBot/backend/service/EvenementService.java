package com.proj_chatBot.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.proj_chatBot.backend.entities.Evenement;
import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.enums.Role;
import com.proj_chatBot.backend.enums.StatutEvenement;
import com.proj_chatBot.backend.repository.EvenementRepository;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EvenementService {
    private static final Logger log = LoggerFactory.getLogger(EvenementService.class);
    private final String uploadDir;
    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private TypeEvenementRepository typeEvenementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    public EvenementService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }
    // Création d'un événement
    public Evenement createEvenement(Evenement evenement, Long utilisateurId) {
        // Validation des dates
        if (evenement.getDateDebut().isAfter(evenement.getDateFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit être avant la date de fin");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        // Vérifiez que l'utilisateur a le droit de créer un événement
        if (!utilisateur.getRole().equals(Role.ADMIN) && !utilisateur.getRole().equals(Role.AGENT_WILAYA)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permissions insuffisantes");
        }

        evenement.setDateCreation(LocalDateTime.now());

        // Associer le type existant
        TypeEvenement type = evenement.getType();
        if (type.getIdType() != null) {
            type = typeEvenementRepository.findById(type.getIdType())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
            evenement.setType(type);
        } else {
            throw new RuntimeException("Type d'événement obligatoire !");
        }

        // Calculer le statut selon les dates de l'événement
        evenement.setStatut(calculerStatut(evenement));

        return evenementRepository.save(evenement);
    }

    // Mise à jour d'un événement
    public Evenement updateEvenement(Long id, Evenement evenementDetails) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        evenement.setTitre(evenementDetails.getTitre());
        evenement.setDescription(evenementDetails.getDescription());
        evenement.setCapaciteMax(evenementDetails.getCapaciteMax());

        // Gérer le type d'événement
        TypeEvenement type = evenementDetails.getType();
        if (type.getIdType() != null) {
            type = typeEvenementRepository.findById(type.getIdType())
                    .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
        } else {
            type = typeEvenementRepository.save(type);
        }
        evenement.setType(type);

        // Mettre à jour les dates et le lieu
        evenement.setDateDebut(evenementDetails.getDateDebut());
        evenement.setDateFin(evenementDetails.getDateFin());
        evenement.setLieu(evenementDetails.getLieu());
        // Mettre à jour le chemin de l'image si fourni
        if (evenementDetails.getImagePath() != null) {
            evenement.setImagePath(evenementDetails.getImagePath());
        }

        // Recalculer le statut
        evenement.setStatut(calculerStatut(evenement));

        return evenementRepository.save(evenement);
    }

    // Méthode utilitaire pour calculer le statut
    public StatutEvenement calculerStatut(Evenement evenement) {
        LocalDateTime now = LocalDateTime.now();
        if (evenement.getDateFin().isBefore(now)) {
            return StatutEvenement.TERMINE;
        } else if (evenement.getDateDebut().isAfter(now)) {
            return StatutEvenement.PROCHAIN;
        } else {
            return StatutEvenement.EN_COURS;
        }
    }

    // Récupérer tous les événements
    public List<Evenement> getAllEvenements(Utilisateur utilisateurConnecte) {
        List<Evenement> evenements;

        if (utilisateurConnecte.getRole() == Role.ADMIN) {
            evenements = evenementRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreation"));
        } else {
            evenements = evenementRepository.findByUtilisateur(utilisateurConnecte,
                    Sort.by(Sort.Direction.DESC, "dateCreation"));
        }

        for (Evenement ev : evenements) {
            ev.setStatut(calculerStatut(ev));
        }
        return evenements;
    }


    // Récupérer un événement par son ID
    public Evenement getEvenementById(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
    }

    // Supprimer un événement par son ID
    public void deleteEvenement(Long id) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        // Supprimer l'image associée
        if (evenement.getImagePath() != null) {
            Path imagePath = Paths.get(uploadDir).resolve(evenement.getImagePath());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                log.error("Échec de la suppression de l'image", e);
            }
        }

        evenementRepository.deleteById(id);
    }


    // Récupérer les événements paginés et filtrés
    public Page<Evenement> getEvenementsFiltresEtPages(Optional<Long> typeId, int page, int size, Utilisateur utilisateurConnecte) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());

        if (utilisateurConnecte.getRole() == Role.ADMIN) {
            if (typeId.isPresent()) {
                TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                        .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
                return evenementRepository.findByType(type, pageable);
            } else {
                return evenementRepository.findAll(pageable);
            }
        } else {
            if (typeId.isPresent()) {
                TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                        .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
                return evenementRepository.findByTypeAndUtilisateur(type, utilisateurConnecte, pageable);
            } else {
                return evenementRepository.findByUtilisateur(utilisateurConnecte, pageable);
            }
        }
    }
    // Compter les événements par type
    public long countEvenementsByType(Optional<Long> typeId, Utilisateur utilisateurConnecte) {
        if (utilisateurConnecte.getRole() == Role.ADMIN) {
            if (typeId.isPresent()) {
                TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                        .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
                return evenementRepository.countByType(type);
            } else {
                return evenementRepository.count();
            }
        } else {
            if (typeId.isPresent()) {
                TypeEvenement type = typeEvenementRepository.findById(typeId.get())
                        .orElseThrow(() -> new RuntimeException("Type d'événement non trouvé"));
                return evenementRepository.countByTypeAndUtilisateur(type, utilisateurConnecte);
            } else {
                return evenementRepository.countByUtilisateur(utilisateurConnecte);
            }
        }
    }
    //gestion des images
    public String storeImage(MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path imagePath = Paths.get(uploadDir).resolve(imageName);
            Files.createDirectories(imagePath.getParent());
            Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            return imageName;
        }
        return null;
    }

    public List<Evenement> getEvenementsPublic() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findByDateFinAfter(now);
    }
}
