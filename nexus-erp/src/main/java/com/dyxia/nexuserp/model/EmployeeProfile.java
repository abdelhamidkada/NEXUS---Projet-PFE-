package com.dyxia.nexuserp.model;

import com.dyxia.nexuserp.util.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

/**
 * Profil d'un employé contenant ses informations professionnelles
 * et ses compétences associées.
 */
@Entity
@Table(name = "employee_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"skills", "documents"})
@EqualsAndHashCode(exclude = {"skills", "documents"})
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String department;

    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 512)
    private String rib;

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeSkill> skills = new HashSet<>();

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<HrDocument> documents = new HashSet<>();
}
