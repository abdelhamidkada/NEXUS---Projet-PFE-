package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Data Transfer Object (DTO) représentant la réponse contenant le token JWT d'authentification,
 * ainsi que les détails de l'utilisateur pour le frontend.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
