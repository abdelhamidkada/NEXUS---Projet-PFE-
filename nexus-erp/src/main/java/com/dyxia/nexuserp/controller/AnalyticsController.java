package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.AnalyticsKpiResponse;
import com.dyxia.nexuserp.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/kpis")
    public ResponseEntity<AnalyticsKpiResponse> getKpis() {
        return ResponseEntity.ok(analyticsService.calculateKpis());
    }
}
