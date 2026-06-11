package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsKpiResponse {
    private Double globalAttritionRisk;
    private Double averageAttendanceRate;
    private Integer trainingAlertsCount;
}
