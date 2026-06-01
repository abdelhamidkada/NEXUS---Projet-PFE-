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
@ToString(exclude = {"skills", "documents", "leaveRequests", "timeTrackings"})
@EqualsAndHashCode(exclude = {"skills", "documents", "leaveRequests", "timeTrackings"})
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

    @Column(unique = true, length = 50)
    private String cin;

    @Column(length = 255)
    private String adresse;

    @Column(length = 50)
    private String contact;

    @Column(name = "type_contrat", length = 50)
    private String typeContrat;

    @Column(name = "date_debut_contrat")
    private java.time.LocalDate dateDebutContrat;

    @Column(name = "duree_contrat")
    private Integer dureeContrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hierarchie_id")
    private User hierarchie;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "signature_numerique", length = 255)
    private String signatureNumerique;

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeSkill> skills = new HashSet<>();

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<HrDocument> documents = new HashSet<>();

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LeaveRequest> leaveRequests = new HashSet<>();

    @Builder.Default
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TimeTracking> timeTrackings = new HashSet<>();
}
