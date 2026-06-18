package com.dyxia.nexuserp.config;

import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de sécurité globale pour Spring Security 6.
 * Définit la chaîne de filtres, les fournisseurs d'authentification et les encodeurs de mots de passe.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter, UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    /**
     * Déclaration du Bean PasswordEncoder utilisant BCrypt avec une force de 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Déclaration du Bean UserDetailsService récupérant les utilisateurs depuis la base de données.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'adresse e-mail : " + username));
    }

    /**
     * Déclaration du Bean AuthenticationProvider configuré avec DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Déclaration du Bean AuthenticationManager pour orchestrer les processus d'authentification.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Point d'entrée d'authentification personnalisé gérant les accès non autorisés
     * en écrivant directement une réponse JSON propre et uniforme.
     */
    @Bean
    public org.springframework.security.web.AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            
            String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Accès non autorisé : %s\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now(),
                authException.getMessage().replace("\"", "\\\""),
                request.getRequestURI()
            );
            
            response.getWriter().write(json);
        };
    }

    /**
     * Configuration de la chaîne de filtres de sécurité (SecurityFilterChain) adaptée à Spring Security 6.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Activer le support CORS avec notre source personnalisée
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Désactivation du CSRF car nous utilisons des jetons JWT (architecture STATELESS)
            .csrf(csrf -> csrf.disable())
            
            // 3. Configuration de la politique de session en STATELESS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Définition des règles d'accès aux URLs :
            //    - Accès public total pour l'authentification (/api/auth/** et /api/v1/auth/**)
            //    - Authentification obligatoire pour tout le reste
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/v1/auth/**", "/api/v1/ai/**", "/api/v1/employees/**", "/api/v1/documents/**", "/api/v1/time-tracking/**", "/api/v1/analytics/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/v1/employee-profiles/**", "/api/v1/hr/profiles/**").permitAll()
                .anyRequest().authenticated()
            )
            
            // 5. Définition du fournisseur d'authentification personnalisé
            .authenticationProvider(authenticationProvider())
            
            // 6. Insertion du filtre JWT juste avant le filtre UsernamePasswordAuthenticationFilter classique de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 7. Configuration du point d'entrée d'exception personnalisé
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint())
            );

        return http.build();
    }

    /**
     * Configuration globale de CORS pour autoriser l'application React en développement et production.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriser le port de développement de Vite React
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 heure de cache pour le preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}