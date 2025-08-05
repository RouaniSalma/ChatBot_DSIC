package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @ManyToOne
    @JoinColumn(name = "division_id")
    @JsonBackReference(value = "division-service")
    private Division division;

    @OneToMany(mappedBy = "service")
    @JsonManagedReference(value = "service-utilisateur")
    private List<Utilisateur> utilisateurs;
}
