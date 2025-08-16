package com.proj_chatBot.backend.repository;

import com.proj_chatBot.backend.DTOs.UtilisateurDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    @Query("SELECT u FROM Utilisateur u WHERE u.service.idService = :serviceId")
    List<Utilisateur> findByServiceId(@Param("serviceId") Long serviceId);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.service s LEFT JOIN FETCH s.division WHERE u.idUtilisateur = :id")
    Optional<Utilisateur> findByIdWithService(@Param("id") Long id);

    @Query("SELECT new com.proj_chatBot.backend.DTOs.UtilisateurDTO(u) FROM Utilisateur u LEFT JOIN FETCH u.service s LEFT JOIN FETCH s.division ORDER BY u.dateCreation DESC")
    Page<UtilisateurDTO> findAllWithServiceAndDivision(Pageable pageable);

    List<Utilisateur> findByService_IdService(Long idService);
}
