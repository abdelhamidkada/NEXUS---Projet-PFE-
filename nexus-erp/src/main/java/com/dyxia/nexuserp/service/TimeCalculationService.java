package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.model.TrackingType;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.TimeTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service de calcul du temps de travail basé sur les pointages.
 */
@Service
@Transactional(readOnly = true)
public class TimeCalculationService {

    private static final double STANDARD_WORK_HOURS_PER_DAY = 7.0;
    private final TimeTrackingRepository timeTrackingRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    /**
     * Constructeur pour injection de dépendances.
     */
    public TimeCalculationService(TimeTrackingRepository timeTrackingRepository,
                                  EmployeeProfileRepository employeeProfileRepository) {
        this.timeTrackingRepository = timeTrackingRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    /**
     * Calcule le rapport quotidien de temps de travail pour un employé spécifié par son ID (Long).
     *
     * @param employeeId L'ID du profil employé.
     * @param date       La date concernée.
     * @return Le DTO contenant le rapport quotidien.
     */
    public DailyTimeReport calculateDailyTime(Long employeeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Récupérer les pointages triés chronologiquement pour la journée donnée
        List<TimeTracking> trackings = timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                employeeId, startOfDay, endOfDay
        );

        long totalSeconds = 0;
        LocalDateTime currentCheckIn = null;
        boolean isMissingCheckout = false;

        for (TimeTracking tracking : trackings) {
            if (tracking.getType() == TrackingType.CHECK_IN) {
                currentCheckIn = tracking.getTimestamp();
            } else if (tracking.getType() == TrackingType.CHECK_OUT) {
                if (currentCheckIn != null) {
                    totalSeconds += Duration.between(currentCheckIn, tracking.getTimestamp()).toSeconds();
                    currentCheckIn = null; // Pointage apparié avec succès
                }
            }
        }

        // Si le dernier pointage est un CHECK_IN sans CHECK_OUT consécutif
        if (currentCheckIn != null) {
            isMissingCheckout = true;
        }

        // Calcul des heures décimales (ex: 7.5 pour 7h30)
        double totalHours = totalSeconds / 3600.0;
        // Arrondi à 2 décimales pour plus de précision et lisibilité
        totalHours = Math.round(totalHours * 100.0) / 100.0;

        // Calcul des heures supplémentaires (au-delà de la base de 7h/jour pour 35h/semaine)
        double overtimeHours = 0.0;
        if (totalHours > STANDARD_WORK_HOURS_PER_DAY) {
            overtimeHours = totalHours - STANDARD_WORK_HOURS_PER_DAY;
            overtimeHours = Math.round(overtimeHours * 100.0) / 100.0;
        }

        return DailyTimeReport.builder()
                .date(date)
                .totalHours(totalHours)
                .overtimeHours(overtimeHours)
                .isMissingCheckout(isMissingCheckout)
                .build();
    }

    /**
     * Calcule le rapport quotidien de temps de travail pour un employé spécifié par son UUID.
     * Effectue une conversion sûre vers le type Long.
     *
     * @param employeeId L'UUID de l'employé.
     * @param date       La date concernée.
     * @return Le DTO contenant le rapport quotidien.
     */
    public DailyTimeReport calculateDailyTime(UUID employeeId, LocalDate date) {
        try {
            Long id = Long.parseLong(employeeId.toString());
            return calculateDailyTime(id, date);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'identifiant de l'employé doit correspondre au type Long utilisé par EmployeeProfile. L'UUID fourni n'est pas convertible : " + employeeId);
        }
    }

    /**
     * Enregistre un pointage de temps (CHECK_IN ou CHECK_OUT) pour un employé (ID Long).
     *
     * @param employeeId L'ID du profil employé.
     * @param tracking   Le pointage à enregistrer.
     * @return Le pointage enregistré.
     */
    @Transactional
    public TimeTracking recordTimeTracking(Long employeeId, TimeTracking tracking) {
        EmployeeProfile employee = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + employeeId));
        tracking.setEmployeeProfile(employee);
        if (tracking.getTimestamp() == null) {
            tracking.setTimestamp(LocalDateTime.now());
        }
        return timeTrackingRepository.save(tracking);
    }

    /**
     * Enregistre un pointage de temps (CHECK_IN ou CHECK_OUT) pour un employé (UUID).
     *
     * @param employeeId L'UUID de l'employé.
     * @param tracking   Le pointage à enregistrer.
     * @return Le pointage enregistré.
     */
    @Transactional
    public TimeTracking recordTimeTracking(UUID employeeId, TimeTracking tracking) {
        try {
            Long id = Long.parseLong(employeeId.toString());
            return recordTimeTracking(id, tracking);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'identifiant de l'employé doit correspondre au type Long utilisé par EmployeeProfile. L'UUID fourni n'est pas convertible : " + employeeId);
        }
    }
}
