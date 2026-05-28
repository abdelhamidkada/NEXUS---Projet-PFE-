package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object representing the 360° view of an Employee Profile.
 * Omit sensitive database details like the raw bank identifier 'rib' for secure viewing.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String department;
    private Set<EmployeeSkillResponse> skills;
    private Set<HrDocumentResponse> documents;
}
