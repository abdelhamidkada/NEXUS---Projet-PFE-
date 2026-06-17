package com.dyxia.nexuserp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour la requête de rafraîchissement de token.
 */
@Data
public class TokenRefreshRequest {

    @NotBlank(message = "Le jeton de rafraîchissement est obligatoire.")
    private String refreshToken;
}
