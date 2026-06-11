package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.dto.MonthlyCycleReport;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.service.TimeCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Contrôleur REST pour la gestion et le calcul des pointages de présence (Time & Attendance).
 */
@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeCalculationService timeCalculationService;

    /**
     * Endpoint POST pour enregistrer un nouveau pointage (CHECK_IN ou CHECK_OUT) pour un employé.
     *
     * @param employeeId L'identifiant du profil de l'employé.
     * @param tracking   Les informations du pointage (latitude, longitude, type).
     * @return Le pointage enregistré avec le statut 201 Created.
     */
    @PostMapping
    public ResponseEntity<TimeTracking> recordTimeTracking(
            @RequestParam Long employeeId,
            @RequestBody TimeTracking tracking) {
        TimeTracking recorded = timeCalculationService.recordTimeTracking(employeeId, tracking);
        return new ResponseEntity<>(recorded, HttpStatus.CREATED);
    }

    /**
     * Endpoint GET pour obtenir le rapport de temps de travail journalier d'un employé.
     *
     * @param employeeId L'identifiant du profil de l'employé.
     * @param date       La date concernée au format YYYY-MM-DD.
     * @return Le DTO DailyTimeReport avec le total des heures et les heures supplémentaires.
     */
    @GetMapping("/report/{employeeId}")
    public ResponseEntity<DailyTimeReport> getDailyTimeReport(
            @PathVariable Long employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyTimeReport report = timeCalculationService.calculateDailyTime(employeeId, date);
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint GET pour obtenir le rapport d'assiduité mensuel (cycle du 16 au 15) contenant la date donnée.
     *
     * @param employeeId L'identifiant du profil de l'employé.
     * @param date       La date au format YYYY-MM-DD (optionnel, aujourd'hui par défaut).
     * @return Le DTO MonthlyCycleReport.
     */
    @GetMapping("/report/monthly/{employeeId}")
    public ResponseEntity<MonthlyCycleReport> getMonthlyCycleReport(
            @PathVariable Long employeeId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        MonthlyCycleReport report = timeCalculationService.calculateMonthlyCycleReport(employeeId, targetDate);
        return ResponseEntity.ok(report);
    }
}
