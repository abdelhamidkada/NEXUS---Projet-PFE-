package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Payload de requête envoyé par un manager pour corriger le pointage d'une journée spécifique.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeCorrectionRequest {
    private Long employeeId;
    private LocalDate date;
    private String checkInTime;
    private String checkOutTime;
    private String payCode;
    private String accrual;
}
