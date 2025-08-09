package com.proj_chatBot.backend.service;

import com.proj_chatBot.backend.DTOs.CreateUtilisateurDTO;
import com.proj_chatBot.backend.DTOs.UtilisateurDTO;
import com.proj_chatBot.backend.entities.ServiceEntity;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.ServiceEntityRepository;
import com.proj_chatBot.backend.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// UtilisateurService.java
@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ServiceEntityRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              ServiceEntityRepository serviceRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
    }




    public UtilisateurDTO createUtilisateur(CreateUtilisateurDTO dto) {
        if(utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(dto.getRole());
        utilisateur.setDateCreation(LocalDateTime.now());

        if(dto.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service non trouvé"));
            utilisateur.setService(service);
        }

        return new UtilisateurDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurDTO updateUtilisateur(Long id, CreateUtilisateurDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if(!utilisateur.getEmail().equals(dto.getEmail())) {
            if(utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email déjà utilisé");
            }
            utilisateur.setEmail(dto.getEmail());
        }

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setRole(dto.getRole());

        if(dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        }

        if(dto.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service non trouvé"));
            utilisateur.setService(service);
        } else {
            utilisateur.setService(null);
        }

        return new UtilisateurDTO(utilisateurRepository.save(utilisateur));
    }

    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }
    public Page<UtilisateurDTO> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return utilisateurRepository.findAllWithServiceAndDivision(pageable);
    }
}