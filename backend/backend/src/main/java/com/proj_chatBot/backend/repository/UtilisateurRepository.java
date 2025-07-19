package com.proj_chatBot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByEmail(String email); // utile pour l'authentification
}
