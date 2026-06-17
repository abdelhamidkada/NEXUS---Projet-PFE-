package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.AuthResponse;
import com.dyxia.nexuserp.dto.LoginRequest;
import com.dyxia.nexuserp.dto.TokenRefreshRequest;
import com.dyxia.nexuserp.dto.TokenRefreshResponse;
import com.dyxia.nexuserp.model.RefreshToken;
import com.dyxia.nexuserp.model.Role;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.repository.RefreshTokenRepository;
import com.dyxia.nexuserp.service.JwtService;
import com.dyxia.nexuserp.service.RefreshTokenService;
import com.dyxia.nexuserp.exception.TokenRefreshException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant les requêtes d'authentification (connexion, rafraîchissement de jeton, etc.).
 */
@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Point d'accès POST permettant de connecter un utilisateur.
     * Authentifie les identifiants puis génère un jeton JWT d'accès et un Refresh Token de session.
     *
     * @param request Le DTO {@link LoginRequest} contenant l'email et le mot de passe.
     * @return Le DTO {@link AuthResponse} contenant le jeton JWT et le Refresh Token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
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

        // 4. Génération du Refresh Token (avec nettoyage automatique des anciens tokens)
        final RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 5. Retour du DTO de réponse avec le jeton et les détails utilisateur
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(token)
                        .refreshToken(refreshToken.getToken())
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream().map(Role::getName).toList())
                        .build()
        );
    }

    /**
     * Point d'accès POST permettant de rafraîchir le jeton d'accès JWT à partir d'un Refresh Token.
     * Implémente la rotation des Refresh Tokens : l'ancien est supprimé, un nouveau est généré.
     *
     * @param request Le DTO {@link TokenRefreshRequest} contenant l'ancien Refresh Token.
     * @return Le DTO {@link TokenRefreshResponse} contenant le nouveau JWT et le nouveau Refresh Token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // 1. Recherche du refresh token dans la base de données
        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Le jeton de rafraîchissement n'est pas présent dans la base de données."));

        // 2. Vérification de l'expiration du jeton (le jette si expiré)
        token = refreshTokenService.verifyExpiration(token);

        // 3. Récupération de l'utilisateur associé
        User user = token.getUser();

        // 4. Rotation du jeton : suppression du jeton actuel
        refreshTokenRepository.delete(token);

        // 5. Génération d'un nouveau couple JWT et Refresh Token
        String newAccessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 6. Retour de la réponse
        return ResponseEntity.ok(
                TokenRefreshResponse.builder()
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken.getToken())
                        .build()
        );
    }
}

