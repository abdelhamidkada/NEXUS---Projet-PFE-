package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.AuthResponse;
import com.dyxia.nexuserp.dto.LoginRequest;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.model.Role;
import com.dyxia.nexuserp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST gérant les requêtes d'authentification (connexion, etc.).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Point d'accès POST permettant de connecter un utilisateur.
     * Authentifie les identifiants puis génère un jeton JWT d'accès en cas de succès.
     *
     * @param request Le DTO {@link LoginRequest} contenant l'email et le mot de passe.
     * @return Le DTO {@link AuthResponse} contenant le jeton JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Authentification de l'utilisateur avec l'AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Chargement des détails de l'utilisateur
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final User user = (User) userDetails;

        // 3. Génération du token JWT d'accès
        final String token = jwtService.generateToken(userDetails);

        // 4. Retour du DTO de réponse avec le jeton et les détails utilisateur
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(token)
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream().map(Role::getName).toList())
                        .build()
        );
    }
}

