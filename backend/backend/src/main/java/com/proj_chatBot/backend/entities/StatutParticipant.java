package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
@Entity
public class StatutParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStatut;

    @Column(length = 100)
    private String libelle;

    @OneToMany(mappedBy = "statut")
    @JsonManagedReference(value = "statut-participant")
    private List<Participant> participants;
}
