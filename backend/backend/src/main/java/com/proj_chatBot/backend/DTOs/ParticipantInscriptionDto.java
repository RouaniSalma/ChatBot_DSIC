package com.proj_chatBot.backend.DTOs;

import com.proj_chatBot.backend.entities.Participant;
import lombok.Data;

@Data
public class ParticipantInscriptionDto {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String signature;
    private Long statutId;
}
