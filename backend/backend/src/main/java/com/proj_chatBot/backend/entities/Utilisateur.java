package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import com.proj_chatBot.backend.enums.Role;
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

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_service")
    @JsonBackReference
    private ServiceEntity service;

    private Date dateCreation;
    private LocalDateTime dernierAcces;

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Evenement> evenements;
}
