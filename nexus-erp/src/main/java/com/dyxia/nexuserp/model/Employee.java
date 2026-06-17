package com.dyxia.nexuserp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modèle représentant un employé pour la génération de documents.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private String fullName;
    private String matricule;
    private String role;
    private String department;
}
