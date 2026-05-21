package com.dyxia.nexuserp.config;

import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Déclaration de BCrypt avec un coût de 12 pour une sécurité robuste
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Chargement de l'utilisateur par e-mail via le UserRepository
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'adresse e-mail : " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation du CSRF car les API REST utilisant des JWT sont protégées par nature
            .csrf(csrf -> csrf.disable())
            
            // Configuration de la politique de session en STATELESS (pas de session HTTP côté serveur)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Définition des règles d'accès aux URLs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll() // Portes ouvertes pour l'authentification (Login/Register)
                .anyRequest().authenticated()                  // Tout le reste de l'ERP nécessite un token valide
            )
            
            // Intégration du filtre JwtAuthenticationFilter avant le filtre d'authentification par défaut
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}