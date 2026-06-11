package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.model.MonthlyCycle;
import com.dyxia.nexuserp.service.LeaveAccrualService;
import com.dyxia.nexuserp.repository.MonthlyCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion et le déclenchement de l'accumulation des congés.
 */
@RestController
@RequestMapping("/api/v1/tracking/accruals")
@RequiredArgsConstructor
public class LeaveAccrualController {

    private final LeaveAccrualService leaveAccrualService;
    private final MonthlyCycleRepository monthlyCycleRepository;

    /**
     * Récupère tous les cycles mensuels enregistrés pour un employé spécifié.
     *
     * @param employeeId L'identifiant de l'employé.
     * @return Liste de cycles mensuels.
     */
    @GetMapping("/cycles/{employeeId}")
    public ResponseEntity<List<MonthlyCycle>> getCycles(@PathVariable Long employeeId) {
        return ResponseEntity.ok(monthlyCycleRepository.findByEmployeeProfileId(employeeId));
    }

    /**
     * Crée et valide immédiatement un cycle mensuel comme 'travaillé' pour les tests.
     *
     * @param employeeId L'identifiant de l'employé.
     * @param startDate  La date de début du cycle.
     * @param endDate    La date de fin du cycle.
     * @return Le cycle créé et validé.
     */
    @PostMapping("/cycles/validate-test")
    public ResponseEntity<MonthlyCycle> createAndValidateCycle(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        MonthlyCycle cycle = leaveAccrualService.createAndValidateTestCycle(employeeId, startDate, endDate);
        return ResponseEntity.ok(cycle);
    }

    /**
     * Déclenche manuellement et immédiatement la tâche planifiée d'accumulation des congés.
     *
     * @return Message de confirmation du traitement.
     */
    @PostMapping("/process")
    public ResponseEntity<String> triggerProcess() {
        leaveAccrualService.processAccruals();
        return ResponseEntity.ok("Traitement de l'accumulation des congés exécuté avec succès.");
    }
}
