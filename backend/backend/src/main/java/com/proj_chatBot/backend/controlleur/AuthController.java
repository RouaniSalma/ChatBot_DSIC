package com.proj_chatBot.backend.controlleur;

import java.util.HashMap;
import java.util.Map;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.UtilisateurRepository;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.security.CustomUserDetailsService;
import com.proj_chatBot.backend.security.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private UtilisateurRepository utilisateurRepository; // <-- Ajoute ceci

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        // Récupère l'utilisateur depuis la base
        Utilisateur utilisateur = utilisateurRepository.findByEmail(authRequest.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(userDetails.getUsername(), role));
        response.put("utilisateur", utilisateur); // tu peux aussi ne mettre que l'id si tu veux
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);
        System.out.println("Token généré: " + token);
        response.put("token", token);
        response.put("utilisateur", utilisateur);

        return response;
    }
}
@Data
class AuthRequest {
    private String email;
    private String password;
    // getters & setters
}

