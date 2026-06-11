package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Rapport d'assiduité mensuel pour un cycle de pointage personnalisé (du 16 au 15).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyCycleReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalHours;
    private Double overtimeHours;
    private Integer daysWorked;
}
