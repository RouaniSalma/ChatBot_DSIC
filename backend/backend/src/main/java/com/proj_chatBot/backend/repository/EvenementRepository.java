package com.proj_chatBot.backend.repository;

import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Evenement;

import java.time.LocalDateTime;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
        Page<Evenement> findByType(TypeEvenement type, Pageable pageable);
        long countByType(TypeEvenement type);

    // Ajoutez cette méthode pour trier par date de création décroissante
    Page<Evenement> findAllByOrderByDateCreationDesc(Pageable pageable);
    Page<Evenement> findByUtilisateur(Utilisateur utilisateur, Pageable pageable);
    Page<Evenement> findByTypeAndUtilisateur(TypeEvenement type, Utilisateur utilisateur, Pageable pageable);
    long countByTypeAndUtilisateur(TypeEvenement type, Utilisateur utilisateur);
    long countByUtilisateur(Utilisateur utilisateur);

    // Ajoutez aussi cette méthode pour le tri
    List<Evenement> findByUtilisateur(Utilisateur utilisateur, Sort sort);
    List<Evenement> findByDateFinAfter(LocalDateTime date);
    }

