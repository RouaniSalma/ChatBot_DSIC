package com.proj_chatBot.backend.entities;
import com.proj_chatBot.backend.enums.StatutEvenement;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvenement;

    @Column(length = 255)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Date dateCreation;
    private Integer capaciteMax;

    @ManyToOne
    @JoinColumn(name = "id_type")
    private TypeEvenement type;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "evenement")
    private List<Participant> participants;

    @Enumerated(EnumType.STRING)
    private StatutEvenement statut;

}
