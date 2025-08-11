package com.proj_chatBot.backend.config;


import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.proj_chatBot.backend.security.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.format.DateTimeFormatter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permettre l'accès public à l'authentification et aux endpoints publics
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/types-evenement/**").permitAll()
                        .requestMatchers("/public").permitAll()
                        .requestMatchers("/api/images/**").permitAll() // Permettre l'accès aux images

                        // Gestion des utilisateurs réservée aux admins
                        .requestMatchers("/api/utilisateurs/**").hasAuthority("ADMIN")

                        // Gestion des événements pour admins et agents
                        .requestMatchers("/api/evenements/**").hasAnyAuthority("ADMIN", "AGENT_WILAYA")

                        // Inscription des participants autorisée sans authentification
                        .requestMatchers(HttpMethod.POST, "/api/participants/inscription/**").permitAll()

                        // Consultation des participants réservée aux admins et agents
                        .requestMatchers("/api/participants/**").hasAnyAuthority("ADMIN", "AGENT_WILAYA")

                        // Autres endpoints
                        .requestMatchers("/api/divisions", "/api/services").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Plus flexible que allowedOrigins
        config.addAllowedHeader("*");
        config.addAllowedMethod("*"); // Autorise toutes les méthodes
        config.addExposedHeader("Authorization"); // Important pour JWT
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


