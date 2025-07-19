package com.proj_chatBot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.TypeEvenement;

public interface TypeEvenementRepository extends JpaRepository<TypeEvenement, Long> {
}
