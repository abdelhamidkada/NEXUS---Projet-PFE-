package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.MonthlyCycle;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.MonthlyCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service pour gérer l'accumulation automatique des soldes de congés.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveAccrualService {

    private final MonthlyCycleRepository monthlyCycleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    /**
     * Tâche planifiée s'exécutant chaque jour à minuit.
     * Récupère tous les cycles mensuels validés comme travaillés qui n'ont pas encore
     * été traités pour l'accumulation des congés, et crédite 2.5 jours aux employés correspondants.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processAccruals() {
        log.info("Démarrage du traitement automatique de l'accumulation des congés...");
        List<MonthlyCycle> pendingCycles = monthlyCycleRepository.findByValidatedAsWorkedTrueAndProcessedForAccrualFalse();
        log.info("{} cycles en attente de traitement trouvés.", pendingCycles.size());

        for (MonthlyCycle cycle : pendingCycles) {
            EmployeeProfile profile = cycle.getEmployeeProfile();
            double currentBalance = profile.getLeaveBalance() != null ? profile.getLeaveBalance() : 0.0;
            
            // Auto-incrément de 2.5 jours
            profile.setLeaveBalance(currentBalance + 2.5);
            cycle.setProcessedForAccrual(true);

            employeeProfileRepository.save(profile);
            monthlyCycleRepository.save(cycle);

            log.info("Accrual traité pour l'employé {} : Nouveau solde = {} jours (Cycle: {} à {})",
                    profile.getId(), profile.getLeaveBalance(), cycle.getStartDate(), cycle.getEndDate());
        }
    }

    /**
     * Permet d'insérer un cycle de test pré-validé pour un employé pour faciliter la démonstration et les tests.
     */
    @Transactional
    public MonthlyCycle createAndValidateTestCycle(Long employeeId, LocalDate startDate, LocalDate endDate) {
        EmployeeProfile profile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Profil employé non trouvé"));

        MonthlyCycle cycle = MonthlyCycle.builder()
                .employeeProfile(profile)
                .startDate(startDate)
                .endDate(endDate)
                .validatedAsWorked(true)
                .processedForAccrual(false)
                .build();

        return monthlyCycleRepository.save(cycle);
    }
}
