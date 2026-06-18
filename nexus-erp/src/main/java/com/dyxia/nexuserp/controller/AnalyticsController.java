package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.AnalyticsKpiResponse;
import com.dyxia.nexuserp.service.AnalyticsService;
import com.dyxia.nexuserp.service.AttritionPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AttritionPredictionService attritionPredictionService;

    @GetMapping("/kpis")
    public ResponseEntity<AnalyticsKpiResponse> getKpis() {
        return ResponseEntity.ok(analyticsService.calculateKpis());
    }

    @GetMapping("/attrition-risk")
    public ResponseEntity<String> getAttritionRisk(
            @RequestParam int age,
            @RequestParam int absencesCount,
            @RequestParam double overtimeHours,
            @RequestParam int performanceScore) {
        String result = attritionPredictionService.predictRisk(age, absencesCount, overtimeHours, performanceScore);
        return ResponseEntity.ok(result);
    }
}
