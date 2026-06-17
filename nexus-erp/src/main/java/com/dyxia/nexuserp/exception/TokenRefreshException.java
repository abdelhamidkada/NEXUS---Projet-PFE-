package com.dyxia.nexuserp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception personnalisée jetée lors de l'échec de la validation d'un Refresh Token
 * (expiration, jeton invalide, etc.).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crée une nouvelle exception de rafraîchissement de jeton.
     *
     * @param token Le jeton concerné par l'erreur.
     * @param message Le message descriptif détaillé de l'erreur.
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Échec du rafraîchissement pour le jeton [%s] : %s", token, message));
    }
}
