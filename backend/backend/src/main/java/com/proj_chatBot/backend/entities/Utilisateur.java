package com.proj_chatBot.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Data
@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    @Column(length = 255, unique = true)
    private String email;

    @Column(length = 255)
    private String motDePasseHash;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 50)
    private String role; // 'admin' ou 'organisateur'

    @ManyToOne
    @JoinColumn(name = "id_service")
    private ServiceEntity service;

    private Date dateCreation;
    private LocalDateTime dernierAcces;

    @OneToMany(mappedBy = "utilisateur")
    private List<Evenement> evenements;
}
