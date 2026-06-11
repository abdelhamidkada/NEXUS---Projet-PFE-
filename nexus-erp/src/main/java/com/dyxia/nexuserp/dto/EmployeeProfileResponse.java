package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object representing the 360° view of an Employee Profile.
 * Fields are split between public [E] (Everyone) and private [P] (Personal).
 * Private fields are null when the requester is not the owner or an authorized role.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileResponse {

    // ── Identity & system fields ────────────────────────────────────────────
    private Long id;
    private Long userId;
    private String matricule;         // [E]
    private String email;             // [E]
    private String firstName;         // [E]
    private String lastName;          // [E]
    private String photoUrl;          // [E]

    // ── Professional info [E] ───────────────────────────────────────────────
    private String jobTitle;          // [E]
    private String department;        // [E]
    private LocalDate hireDate;       // [E]
    private String timeInJob;         // [E] Calculated field e.g. "2 ans, 3 mois"
    private String workModel;         // [E] WFH / OFFICE / HYBRID
    private String location;          // [E]
    private String spokenLanguages;   // [E]

    // ── Hierarchy [E] ──────────────────────────────────────────────────────
    private Long hierarchieId;
    private String managerName;       // [E]
    private String managerRole;       // [E]
    private String managerMatricule;  // [E]

    // ── Private / Contractual fields [P] ───────────────────────────────────
    // These fields are null unless requester is owner, HR_ADMIN or DIRECTION
    private String cin;               // [P] (identity card)
    private String adresse;           // [P]
    private String contact;           // [P]
    private String typeContrat;       // [P]
    private LocalDate dateDebutContrat; // [P]
    private Integer dureeContrat;     // [P]
    private String payFrequency;      // [P]
    private String employmentFraction; // [P]
    private String seniorityLevel;    // [P]
    private String signatureNumerique; // [P]
    private Double leaveBalance;       // [E]

    // ── Collections ────────────────────────────────────────────────────────
    private Set<EmployeeSkillResponse> skills;
    private Set<HrDocumentResponse> documents;
}
