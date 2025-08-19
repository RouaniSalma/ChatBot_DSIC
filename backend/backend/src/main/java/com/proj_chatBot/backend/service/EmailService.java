package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.name}")
    private String appName;

    @Value("${app.support.email}")
    private String supportEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // Ici, on met le nom de l'app et une adresse générique fictive
            message.setFrom(String.format("%s <%s>", appName, "no-reply@dummy.com"));

            message.setTo(toEmail);
            message.setSubject("Réinitialisation de mot de passe");

            String text = "Lien de réinitialisation : " + resetLink;
            message.setText(text);

            System.out.println("Tentative d'envoi à : " + toEmail);
            mailSender.send(message);
            System.out.println("Email envoyé avec succès !");
        } catch (Exception e) {
            System.err.println("Échec d'envoi : " + e.getMessage());
            throw new RuntimeException("Erreur SMTP : " + e.getMessage());
        }
    }

}
