package com.proj_chatBot.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParticipant;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String signature;

    @ManyToOne
    @JoinColumn(name = "id_statut")
    private StatutParticipant statut;

    @ManyToOne
    @JoinColumn(name = "id_evenement")
    private Evenement evenement;
}
