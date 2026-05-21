package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité de jointure représentant la relation de compétence acquise par un employé,
 * contenant son niveau de maîtrise (proficiency level).
 */
@Entity
@Table(name = "employee_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"employeeProfile", "skill"})
@EqualsAndHashCode(exclude = {"employeeProfile", "skill"})
public class EmployeeSkill {

    @EmbeddedId
    private EmployeeSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeeProfileId")
    @JoinColumn(name = "employee_profile_id")
    private EmployeeProfile employeeProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "proficiency_level", nullable = false)
    private Integer proficiencyLevel;
}
