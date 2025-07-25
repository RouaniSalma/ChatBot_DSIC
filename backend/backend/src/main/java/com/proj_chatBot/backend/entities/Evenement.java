package com.proj_chatBot.backend.entities;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.proj_chatBot.backend.enums.StatutEvenement;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Evenement {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long idEvenement;

        private Date dateCreation;

        private String titre;
        private String description;
        private Integer capaciteMax;
        private String lieu;
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;

        @ManyToOne
        @JoinColumn(name = "id_type")
        private TypeEvenement type;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Participant> participants;

    @Enumerated(EnumType.STRING)
    private StatutEvenement statut;

}
