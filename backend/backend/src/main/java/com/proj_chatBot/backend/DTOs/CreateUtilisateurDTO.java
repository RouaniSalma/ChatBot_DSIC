package com.proj_chatBot.backend.DTOs;

import com.proj_chatBot.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUtilisateurDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String motDePasse;

    private Role role = Role.AGENT_WILAYA;

    private Long serviceId;
}
