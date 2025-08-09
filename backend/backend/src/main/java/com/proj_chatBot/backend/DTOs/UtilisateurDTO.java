package com.proj_chatBot.backend.DTOs;

import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.enums.Role;
import lombok.Data;

@Data
public class UtilisateurDTO {
    private Long idUtilisateur;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private Long serviceId;
    private String serviceNom;
    private String divisionNom;

    // Constructeur à partir de l'entité
    public UtilisateurDTO(Utilisateur utilisateur) {
        this.idUtilisateur = utilisateur.getIdUtilisateur();
        this.email = utilisateur.getEmail();
        this.nom = utilisateur.getNom();
        this.prenom = utilisateur.getPrenom();
        this.role = utilisateur.getRole();
        if(utilisateur.getService() != null) {
            this.serviceId = utilisateur.getService().getIdService();
            this.serviceNom = utilisateur.getService().getIntitule();
            if(utilisateur.getService().getDivision() != null) {
                this.divisionNom = utilisateur.getService().getDivision().getNom();
            }
        }
    }
}
