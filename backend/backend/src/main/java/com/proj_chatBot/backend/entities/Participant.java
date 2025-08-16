package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participant")
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
    @JsonIncludeProperties({"idStatut", "libelle"})
    private StatutParticipant statut;

    @ManyToOne
    @JoinColumn(name = "id_evenement")
    @JsonBackReference(value = "evenement-participant")
    private Evenement evenement;
}
