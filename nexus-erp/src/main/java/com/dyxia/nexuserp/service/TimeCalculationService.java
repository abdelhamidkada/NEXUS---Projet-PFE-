package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.dto.MonthlyCycleReport;
import com.dyxia.nexuserp.dto.DailyAttendanceDetail;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.model.TrackingType;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.TimeTrackingRepository;
import com.dyxia.nexuserp.repository.LeaveRequestRepository;
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

    private static final int SHIFT_START_HOUR = 8;
    private static final int SHIFT_END_HOUR = 16;
    private static final int OVERTIME_END_HOUR = 20;
    private static final java.time.format.DateTimeFormatter TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    private final TimeTrackingRepository timeTrackingRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    /**
     * Constructeur pour injection de dépendances.
     */
    public TimeCalculationService(TimeTrackingRepository timeTrackingRepository,
                                  EmployeeProfileRepository employeeProfileRepository,
                                  LeaveRequestRepository leaveRequestRepository) {
        this.timeTrackingRepository = timeTrackingRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * Calcule le rapport quotidien de temps de travail pour un employé spécifié par son ID (Long).
     * Les heures normales sont comprises entre 08:00 et 16:00.
     * Les heures supplémentaires sont comprises entre 16:00 et 20:00.
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

        long regularSeconds = 0;
        long overtimeSeconds = 0;
        long nightSeconds = 0;
        LocalDateTime currentCheckIn = null;
        boolean isMissingCheckout = false;

        LocalDateTime coreStart = date.atTime(SHIFT_START_HOUR, 0);
        LocalDateTime coreEnd = date.atTime(SHIFT_END_HOUR, 0);
        LocalDateTime overtimeStart = date.atTime(SHIFT_END_HOUR, 0);
        LocalDateTime overtimeEnd = date.atTime(OVERTIME_END_HOUR, 0);

        for (TimeTracking tracking : trackings) {
            if (tracking.getType() == TrackingType.CHECK_IN) {
                currentCheckIn = tracking.getTimestamp();
            } else if (tracking.getType() == TrackingType.CHECK_OUT) {
                if (currentCheckIn != null) {
                    LocalDateTime checkIn = currentCheckIn;
                    LocalDateTime checkOut = tracking.getTimestamp();

                    // Calcul de l'intersection avec la plage standard [08:00, 16:00]
                    LocalDateTime regStart = checkIn.isBefore(coreStart) ? coreStart : (checkIn.isAfter(coreEnd) ? coreEnd : checkIn);
                    LocalDateTime regEnd = checkOut.isBefore(coreStart) ? coreStart : (checkOut.isAfter(coreEnd) ? coreEnd : checkOut);
                    if (regStart.isBefore(regEnd)) {
                        regularSeconds += Duration.between(regStart, regEnd).toSeconds();
                    }

                    // Calcul de l'intersection avec la plage d'heures supplémentaires [16:00, 20:00]
                    LocalDateTime ovStart = checkIn.isBefore(overtimeStart) ? overtimeStart : (checkIn.isAfter(overtimeEnd) ? overtimeEnd : checkIn);
                    LocalDateTime ovEnd = checkOut.isBefore(overtimeStart) ? overtimeStart : (checkOut.isAfter(overtimeEnd) ? overtimeEnd : checkOut);
                    if (ovStart.isBefore(ovEnd)) {
                        overtimeSeconds += Duration.between(ovStart, ovEnd).toSeconds();
                    }

                    // Calcul des heures de nuit [21:00, 06:00]
                    Duration nightDuration = calculateNightHours(checkIn, checkOut);
                    nightSeconds += nightDuration.toSeconds();

                    currentCheckIn = null; // Pointage apparié avec succès
                }
            }
        }

        // Si le dernier pointage est un CHECK_IN sans CHECK_OUT consécutif
        if (currentCheckIn != null) {
            isMissingCheckout = true;
        }

        // Calcul des heures décimales
        double regularHours = regularSeconds / 3600.0;
        double overtimeHours = overtimeSeconds / 3600.0;
        double nightHours = nightSeconds / 3600.0;

        // Arrondi à 2 décimales
        regularHours = Math.round(regularHours * 100.0) / 100.0;
        overtimeHours = Math.round(overtimeHours * 100.0) / 100.0;
        nightHours = Math.round(nightHours * 100.0) / 100.0;

        double totalHours = regularHours + overtimeHours + nightHours;
        totalHours = Math.round(totalHours * 100.0) / 100.0;

        return DailyTimeReport.builder()
                .date(date)
                .totalHours(totalHours)
                .overtimeHours(overtimeHours)
                .nightHours(nightHours)
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
     * Détermine la plage de dates pour le cycle mensuel contenant la date fournie.
     * Le cycle mensuel s'étend du 16 du mois N au 15 du mois N+1.
     *
     * @param date La date pour laquelle trouver le cycle.
     * @return Tableau de deux LocalDate [dateDebut, dateFin] du cycle.
     */
    public LocalDate[] getMonthlyCycleRange(LocalDate date) {
        if (date.getDayOfMonth() >= 16) {
            LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 16);
            LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(15);
            return new LocalDate[]{startDate, endDate};
        } else {
            LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), 15);
            LocalDate startDate = endDate.minusMonths(1).withDayOfMonth(16);
            return new LocalDate[]{startDate, endDate};
        }
    }

    /**
     * Calcule le rapport mensuel personnalisé (du 16 au 15) pour un employé et une date cible.
     *
     * @param employeeId L'ID de l'employé.
     * @param date Une date dans le cycle mensuel recherché.
     * @return Le rapport mensuel.
     */
    public MonthlyCycleReport calculateMonthlyCycleReport(Long employeeId, LocalDate date) {
        LocalDate[] range = getMonthlyCycleRange(date);
        LocalDate start = range[0];
        LocalDate end = range[1];

        double totalHours = 0.0;
        double overtimeHours = 0.0;
        int daysWorked = 0;
        java.util.List<DailyAttendanceDetail> details = new java.util.ArrayList<>();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            DailyTimeReport daily = calculateDailyTime(employeeId, current);
            
            String checkInTime = null;
            String checkOutTime = null;
            Double hoursWorked = daily.getTotalHours();
            Double dailyOvertimeHours = daily.getOvertimeHours();
            Double dailyNightHours = daily.getNightHours();
            String status = null;
            UUID trackingId = null;

            List<LeaveRequest> leaves = leaveRequestRepository.findValidatedLeavesForToday(employeeId, current);
            if (!leaves.isEmpty()) {
                status = "Congé";
                hoursWorked = 0.0;
                dailyOvertimeHours = 0.0;
                dailyNightHours = 0.0;
            } else {
                LocalDateTime startOfDay = current.atStartOfDay();
                LocalDateTime endOfDay = current.plusDays(1).atStartOfDay();
                List<TimeTracking> trackings = timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                        employeeId, startOfDay, endOfDay
                );
                TimeTracking checkIn = trackings.stream()
                        .filter(t -> t.getType() == TrackingType.CHECK_IN)
                        .findFirst().orElse(null);
                TimeTracking checkOut = trackings.stream()
                        .filter(t -> t.getType() == TrackingType.CHECK_OUT)
                        .findFirst().orElse(null);

                if (checkIn != null) {
                    trackingId = checkIn.getId();
                    checkInTime = checkIn.getTimestamp().toLocalTime().format(TIME_FORMATTER);
                    status = checkIn.getAttendanceStatus();
                    if (status == null || status.trim().isEmpty()) {
                        status = "Normal";
                    }
                    if (checkOut != null) {
                        checkOutTime = checkOut.getTimestamp().toLocalTime().format(TIME_FORMATTER);
                    }
                } else {
                    java.time.DayOfWeek dow = current.getDayOfWeek();
                    if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
                        status = "Week-end";
                    } else {
                        status = "Absent";
                    }
                }
            }

            if (hoursWorked > 0 || daily.isMissingCheckout()) {
                totalHours += daily.getTotalHours();
                overtimeHours += daily.getOvertimeHours();
                daysWorked++;
            }

            details.add(DailyAttendanceDetail.builder()
                    .date(current)
                    .checkInTime(checkInTime)
                    .checkOutTime(checkOutTime)
                    .hoursWorked(hoursWorked)
                    .overtimeHours(dailyOvertimeHours)
                    .nightHours(dailyNightHours)
                    .status(status)
                    .trackingId(trackingId)
                    .build());

            current = current.plusDays(1);
        }

        totalHours = Math.round(totalHours * 100.0) / 100.0;
        overtimeHours = Math.round(overtimeHours * 100.0) / 100.0;

        return MonthlyCycleReport.builder()
                .startDate(start)
                .endDate(end)
                .totalHours(totalHours)
                .overtimeHours(overtimeHours)
                .daysWorked(daysWorked)
                .dailyDetails(details)
                .build();
    }

    /**
     * Calcule le rapport mensuel personnalisé pour un employé spécifié par son UUID.
     *
     * @param employeeId L'UUID de l'employé.
     * @param date Une date dans le cycle mensuel recherché.
     * @return Le rapport mensuel.
     */
    public MonthlyCycleReport calculateMonthlyCycleReport(UUID employeeId, LocalDate date) {
        try {
            Long id = Long.parseLong(employeeId.toString());
            return calculateMonthlyCycleReport(id, date);
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
        
        // Rule 2: Strict 5-minute tolerance check on check-in
        if (tracking.getType() == TrackingType.CHECK_IN) {
            java.time.LocalTime checkInTime = tracking.getTimestamp().toLocalTime();
            int hour = checkInTime.getHour();
            int minute = checkInTime.getMinute();
            if (hour > SHIFT_START_HOUR || (hour == SHIFT_START_HOUR && minute > 5)) {
                tracking.setAttendanceStatus("Absence injustifiée");
            } else {
                tracking.setAttendanceStatus("Normal");
            }
        }
        
        return timeTrackingRepository.save(tracking);
    }

    /**
     * Permet à un administrateur RH d'outrepasser une absence injustifiée en absence justifiée (Maladie).
     *
     * @param trackingId L'ID du pointage à modifier.
     * @return Le pointage mis à jour.
     */
    @Transactional
    public TimeTracking overrideLatePunchIn(UUID trackingId) {
        TimeTracking tracking = timeTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Pointage non trouvé avec l'ID : " + trackingId));
        if (!"Absence injustifiée".equals(tracking.getAttendanceStatus())) {
            throw new IllegalArgumentException("Seuls les pointages avec le statut 'Absence injustifiée' peuvent être modifiés.");
        }
        tracking.setAttendanceStatus("Absence justifiée (Maladie)");
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

    /**
     * Calcule la durée exacte d'intersection d'une période de travail avec la plage horaire de nuit.
     * Plage de nuit définie de 21:00 à 06:00 selon les standards français.
     *
     * @param clockIn  Date et heure de début du shift.
     * @param clockOut Date et heure de fin du shift.
     * @return La durée travaillée pendant la nuit.
     */
    public Duration calculateNightHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockIn == null || clockOut == null || !clockIn.isBefore(clockOut)) {
            return Duration.ZERO;
        }

        Duration totalNightDuration = Duration.ZERO;
        LocalDate startDate = clockIn.toLocalDate();
        LocalDate endDate = clockOut.toLocalDate();

        // On vérifie les fenêtres de nuit du jour précédent (au cas où le shift commence avant 06:00) au jour de fin
        for (LocalDate date = startDate.minusDays(1); !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime windowStart = date.atTime(21, 0);
            LocalDateTime windowEnd = date.plusDays(1).atTime(6, 0);

            LocalDateTime overlapStart = clockIn.isAfter(windowStart) ? clockIn : windowStart;
            LocalDateTime overlapEnd = clockOut.isBefore(windowEnd) ? clockOut : windowEnd;

            if (overlapStart.isBefore(overlapEnd)) {
                totalNightDuration = totalNightDuration.plus(Duration.between(overlapStart, overlapEnd));
            }
        }

        return totalNightDuration;
    }
}
