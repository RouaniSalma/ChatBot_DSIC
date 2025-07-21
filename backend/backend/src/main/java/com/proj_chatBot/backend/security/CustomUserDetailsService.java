package com.proj_chatBot.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import com.proj_chatBot.backend.entities.Utilisateur;
import com.proj_chatBot.backend.repository.UtilisateurRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé");
        }

        // Si getRole() retourne un Enum, utilise .name()
        return User.builder()
                .username(user.getEmail())
                .password(user.getMotDePasseHash())
                .authorities(new SimpleGrantedAuthority(user.getRole().name())) // authority: 'ADMIN'
                .build();
    }
}
