package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.Data;
import jakarta.persistence.*;

import java.util.List;

@Data
@Entity
public class Division {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDivision;

    @Column(length = 255)
    private String nom;

    @Column(length = 50)
    private String abbreviation;

    @OneToMany(mappedBy = "division")
    @JsonManagedReference(value = "division-service")
    private List<ServiceEntity> services;
}
