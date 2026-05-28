package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing an HR document stored in the Digital HR Vault.
 */
@Entity
@Table(name = "hr_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employeeProfile")
@EqualsAndHashCode(exclude = "employeeProfile")
public class HrDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_signed", nullable = false)
    private Boolean isSigned;

    @PrePersist
    protected void onCreate() {
        if (this.uploadDate == null) {
            this.uploadDate = LocalDateTime.now();
        }
        if (this.isSigned == null) {
            this.isSigned = false;
        }
    }
}
