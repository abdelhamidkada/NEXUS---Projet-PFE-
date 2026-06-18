package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.dto.MonthlyCycleReport;
import com.dyxia.nexuserp.dto.TimeCorrectionRequest;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.service.TimeCalculationService;
import com.dyxia.nexuserp.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Contrôleur REST pour la gestion et le calcul des pointages de présence (Time & Attendance) et QR codes.
 */
@RestController
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeCalculationService timeCalculationService;
    private final QrCodeService qrCodeService;

    /**
     * Endpoint POST pour enregistrer un nouveau pointage (CHECK_IN ou CHECK_OUT) pour un employé.
     */
    @PostMapping("/api/v1/tracking")
    public ResponseEntity<TimeTracking> recordTimeTracking(
            @RequestParam Long employeeId,
            @RequestBody TimeTracking tracking) {
        TimeTracking recorded = timeCalculationService.recordTimeTracking(employeeId, tracking);
        return new ResponseEntity<>(recorded, HttpStatus.CREATED);
    }

    /**
     * Endpoint GET pour obtenir le rapport de temps de travail journalier d'un employé.
     */
    @GetMapping("/api/v1/tracking/report/{employeeId}")
    public ResponseEntity<DailyTimeReport> getDailyTimeReport(
            @PathVariable Long employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyTimeReport report = timeCalculationService.calculateDailyTime(employeeId, date);
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint GET pour obtenir le rapport d'assiduité mensuel.
     */
    @GetMapping("/api/v1/tracking/report/monthly/{employeeId}")
    public ResponseEntity<MonthlyCycleReport> getMonthlyCycleReport(
            @PathVariable Long employeeId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        MonthlyCycleReport report = timeCalculationService.calculateMonthlyCycleReport(employeeId, targetDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint POST permettant à un utilisateur RH d'outrepasser le statut late d'un pointage.
     */
    @PostMapping("/api/v1/tracking/override/{trackingId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('HR_ADMIN', 'DIRECTION')")
    public ResponseEntity<TimeTracking> overrideLatePunchIn(@PathVariable java.util.UUID trackingId) {
        TimeTracking updated = timeCalculationService.overrideLatePunchIn(trackingId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Endpoint GET pour générer un QR Code de pointage physique pour un collaborateur.
     */
    @GetMapping(value = "/api/v1/time-tracking/qr-code/{matricule}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateEmployeeQrCode(@PathVariable String matricule) {
        byte[] qrCodeImage = qrCodeService.generateEmployeeQrCode(matricule);
        return ResponseEntity.ok(qrCodeImage);
    }

    /**
     * Endpoint PUT permettant à un manager de corriger des pointages pour un employé et une date donnée.
     */
    @PutMapping("/api/v1/tracking/correct")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER', 'HR_ADMIN', 'DIRECTION')")
    public ResponseEntity<Void> correctTimeTracking(@RequestBody TimeCorrectionRequest request) {
        timeCalculationService.correctTimeTracking(request);
        return ResponseEntity.ok().build();
    }
}
