package com.proj_chatBot.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class TypeEvenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idType;

    @Column(length = 100)
    private String nom;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Column(length = 255)
    private String lieu;

    @OneToMany(mappedBy = "type")
    private List<Evenement> evenements;
}