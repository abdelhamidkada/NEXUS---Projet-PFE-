package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un rôle d'utilisateur au sein de l'application Nexus ERP.
 * Mappe la table 'roles' définie dans le schéma de base de données.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
