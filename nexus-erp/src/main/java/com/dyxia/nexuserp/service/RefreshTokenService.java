package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.exception.TokenRefreshException;
import com.dyxia.nexuserp.model.RefreshToken;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.repository.RefreshTokenRepository;
import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service gérant la logique métier des Refresh Tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Crée un nouveau Refresh Token pour un utilisateur donné.
     * Supprime d'abord les jetons existants de cet utilisateur pour éviter l'accumulation.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Le RefreshToken créé et sauvegardé.
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        // Supprime les jetons de rafraîchissement précédents de l'utilisateur (mécanisme de session propre)
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Vérifie si un Refresh Token a expiré.
     * Si expiré, le supprime de la base de données et jette une TokenRefreshException.
     *
     * @param token Le jeton de rafraîchissement à valider.
     * @return Le jeton s'il est encore valide.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Le jeton de rafraîchissement a expiré. Veuillez vous reconnecter.");
        }
        return token;
    }

    /**
     * Supprime le Refresh Token d'un utilisateur par son ID.
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
