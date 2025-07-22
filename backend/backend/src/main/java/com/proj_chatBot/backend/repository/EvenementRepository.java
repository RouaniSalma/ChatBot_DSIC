package com.proj_chatBot.backend.repository;

import com.proj_chatBot.backend.entities.TypeEvenement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Evenement;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
        Page<Evenement> findByType(TypeEvenement type, Pageable pageable);
        long countByType(TypeEvenement type);
    }

