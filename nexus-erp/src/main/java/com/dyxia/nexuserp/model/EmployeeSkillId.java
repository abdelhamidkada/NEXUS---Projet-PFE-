package com.dyxia.nexuserp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité de jointure EmployeeSkill.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkillId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "employee_profile_id")
    private Long employeeProfileId;

    @Column(name = "skill_id")
    private Long skillId;
}
