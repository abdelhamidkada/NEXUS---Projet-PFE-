package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an employee's professional skill in the 360° profile view.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSkillResponse {
    private Long skillId;
    private String name;
    private String category;
    private Integer proficiencyLevel;
}
