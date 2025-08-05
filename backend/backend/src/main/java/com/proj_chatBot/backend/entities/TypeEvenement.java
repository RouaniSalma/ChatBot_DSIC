package com.proj_chatBot.backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.proj_chatBot.backend.enums.Role;
import com.proj_chatBot.backend.enums.TypeEvent;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class TypeEvenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idType;

    @Enumerated(EnumType.STRING)
    private TypeEvent typeEvent; // Enum: RÉUNION, CONFÉRENCE, etc.

    @OneToMany(mappedBy = "type")
    @JsonIgnoreProperties("type")
    private List<Evenement> evenements;
}