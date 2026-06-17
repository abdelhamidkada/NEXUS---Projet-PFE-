package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
2 * Entité JPA représentant un Refresh Token dans le système d'authentification.
3 * Permet de générer de nouveaux jetons d'accès JWT de façon sécurisée sans forcer
4 * l'utilisateur à ressaisir ses identifiants.
5 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
