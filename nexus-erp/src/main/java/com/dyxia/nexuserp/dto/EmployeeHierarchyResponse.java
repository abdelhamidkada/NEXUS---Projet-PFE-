package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a node in the hierarchical chain of command.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeHierarchyResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String department;
    private String photoUrl;
    private EmployeeHierarchyResponse manager;
}
