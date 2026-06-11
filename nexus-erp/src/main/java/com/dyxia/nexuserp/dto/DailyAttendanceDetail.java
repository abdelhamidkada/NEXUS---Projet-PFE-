package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Détail d'assiduité journalier pour le rapport mensuel.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyAttendanceDetail {
    private LocalDate date;
    private String checkInTime;
    private String checkOutTime;
    private Double hoursWorked;
    private Double overtimeHours;
    private String status;
    private UUID trackingId;
}
