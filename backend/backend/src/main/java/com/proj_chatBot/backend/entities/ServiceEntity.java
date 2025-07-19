package com.proj_chatBot.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
@Entity
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idService;

    @Column(length = 255)
    private String intitule;

    @Column(length = 50)
    private String abbreviation;

    @OneToMany(mappedBy = "service")
    private List<Utilisateur> utilisateurs;
}
