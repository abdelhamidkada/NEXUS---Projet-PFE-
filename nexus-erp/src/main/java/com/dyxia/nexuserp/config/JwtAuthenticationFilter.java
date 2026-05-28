package com.dyxia.nexuserp.config;

import com.dyxia.nexuserp.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre de sécurité interceptant chaque requête HTTP pour valider le token JWT s'il est présent.
 * Étend {@link OncePerRequestFilter} pour garantir une seule exécution par requête.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Étape 1 : Vérification de la présence du header Authorization avec le préfixe "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Étape 2 : Extraction et validation sécurisée du token JWT
        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            // Étape 3 : Si l'email est présent et que l'utilisateur n'est pas encore authentifié dans le contexte
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Étape 4 : Validation du token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    // Enrichissement du token avec les détails de la requête HTTP
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Étape 5 : Injection du token d'authentification dans le contexte de sécurité Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas de token expiré, malformé ou signature invalide, on log l'avertissement
            // et on laisse le filtre continuer sans injecter d'authentification.
            // Spring Security rejettera naturellement les endpoints sécurisés avec un code 401.
            logger.warn("Échec de la validation du token JWT : " + e.getMessage());
        }
        
        // Poursuite de la chaîne de filtres de sécurité
        filterChain.doFilter(request, response);
    }
}
