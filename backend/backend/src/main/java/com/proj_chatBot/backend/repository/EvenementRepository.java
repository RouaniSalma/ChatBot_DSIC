package com.proj_chatBot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Evenement;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
}
