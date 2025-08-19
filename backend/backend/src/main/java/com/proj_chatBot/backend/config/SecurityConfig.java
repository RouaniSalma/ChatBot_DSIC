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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Autoriser l'inscription sans authentification
                        .requestMatchers("/api/participants/inscription/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/participants/disponibilite/**").permitAll()
                        // Les autres endpoints participants protégés
                        .requestMatchers(HttpMethod.GET, "/api/participants/**").hasAnyAuthority("ADMIN", "AGENT_WILAYA")
                        .requestMatchers(HttpMethod.DELETE, "/api/participants/**").hasAnyAuthority("ADMIN", "AGENT_WILAYA")

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
                        // Gestion des participants (lecture/suppression)

                        // Autres endpoints
                        .requestMatchers("/api/divisions", "/api/services").authenticated()
                        .requestMatchers("/api/statuts-participant/**").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Supprimez WebConfig et gardez seulement la configuration dans SecurityConfig
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // ou votre port frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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


