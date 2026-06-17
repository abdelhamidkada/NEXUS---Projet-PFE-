package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Rapport journalier du temps de travail et des heures supplémentaires.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTimeReport {
    private LocalDate date;
    private Double totalHours;
    private Double overtimeHours;
    private Double nightHours;
    private boolean isMissingCheckout;
}
