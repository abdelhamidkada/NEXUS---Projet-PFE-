package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de transfert de données (DTO) pour la réponse de rafraîchissement de token.
 * Contient le nouveau jeton d'accès (JWT) et le nouveau Refresh Token de rotation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponse {

    private String token;
    private String refreshToken;
}
