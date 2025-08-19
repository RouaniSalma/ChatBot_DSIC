package com.proj_chatBot.backend.controlleur;

import java.time.LocalDateTime;
import java.util.*;

import com.proj_chatBot.backend.DTOs.ForgotPasswordRequest;
import com.proj_chatBot.backend.DTOs.ResetPasswordRequest;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.security.CustomUserDetailsService;
import com.proj_chatBot.backend.security.JwtUtil;
import com.proj_chatBot.backend.service.EmailService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        // Correction: Utilisation de orElseThrow pour gérer l'Optional
        Utilisateur utilisateur = utilisateurRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(userDetails.getUsername(), role));
        response.put("utilisateur", utilisateur);

        String token = jwtUtil.generateToken(userDetails.getUsername(), role);
        System.out.println("Token généré: " + token);
        response.put("token", token);
        response.put("utilisateur", utilisateur);

        return response;
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        System.out.println("Demande de réinitialisation pour: " + request.getEmail());

        Optional<Utilisateur> userOptional = utilisateurRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            Utilisateur user = userOptional.get();
            System.out.println("Utilisateur trouvé: " + user.getEmail());

            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            utilisateurRepository.save(user);

            String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
            System.out.println("Lien de réinitialisation généré: " + resetLink);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
                System.out.println("Email envoyé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'email");
            }
        } else {
            System.out.println("Aucun utilisateur trouvé avec cet email");
        }

        return ResponseEntity.ok().build();
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<Utilisateur> userOptional = utilisateurRepository.findByResetToken(request.getToken());

        if (userOptional.isEmpty() || userOptional.get().getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré");
        }

        Utilisateur user = userOptional.get();
        user.setMotDePasseHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        utilisateurRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
@Data
class AuthRequest {
    private String email;
    private String password;
    // getters & setters
}

