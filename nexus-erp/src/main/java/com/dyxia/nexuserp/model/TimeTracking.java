package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un pointage de temps pour l'assiduité.
 */
@Entity
@Table(name = "time_trackings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employeeProfile")
@EqualsAndHashCode(exclude = "employeeProfile")
public class TimeTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TrackingType type;

    private Double latitude;

    private Double longitude;

    @Column(name = "attendance_status", length = 50)
    private String attendanceStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;
}
