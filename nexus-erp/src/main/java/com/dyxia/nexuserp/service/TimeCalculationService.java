package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.dto.MonthlyCycleReport;
import com.dyxia.nexuserp.dto.DailyAttendanceDetail;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.LeaveType;
import com.dyxia.nexuserp.model.LeaveStatus;
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

        return calculateDailyTimeInMemory(employeeId, date, trackings);
    }

    private DailyTimeReport calculateDailyTimeInMemory(Long employeeId, LocalDate date, List<TimeTracking> trackings) {
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
        return com.dyxia.nexuserp.util.DateCycleUtils.getMonthlyCycleRange(date);
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

        // Fetch all validated leaves for the cycle in one query
        List<LeaveRequest> rangeLeaves = leaveRequestRepository.findValidatedLeavesForRange(employeeId, start, end);

        // Fetch all trackings for the cycle in one query
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
        List<TimeTracking> rangeTrackings = timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                employeeId, startDateTime, endDateTime
        );

        EmployeeProfile profile = employeeProfileRepository.findById(employeeId).orElse(null);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            // Filter trackings for the current day in memory
            LocalDate finalCurrent = current;
            List<TimeTracking> dayTrackings = rangeTrackings.stream()
                    .filter(t -> t.getTimestamp().toLocalDate().equals(finalCurrent))
                    .sorted(java.util.Comparator.comparing(TimeTracking::getTimestamp))
                    .collect(java.util.stream.Collectors.toList());

            // Check if there are validated leaves for this day
            LeaveRequest leave = rangeLeaves.stream()
                    .filter(lr -> !finalCurrent.isBefore(lr.getStartDate()) && !finalCurrent.isAfter(lr.getEndDate()))
                    .findFirst().orElse(null);

            DailyTimeReport daily = calculateDailyTimeInMemory(employeeId, finalCurrent, dayTrackings);
            
            String checkInTime = null;
            String checkOutTime = null;
            Double hoursWorked = daily.getTotalHours();
            Double dailyOvertimeHours = daily.getOvertimeHours();
            Double dailyNightHours = daily.getNightHours();
            String status = null;
            UUID trackingId = null;
            String dailyLocation = null;
            String payCode = null;
            String accrualStr = "—";

            if (leave != null) {
                if (leave.getType() == LeaveType.SICK) {
                    status = "Absence justifiée (Maladie)";
                    payCode = "M - Maladie";
                } else {
                    status = "Congé";
                    payCode = "C - Congés Payés";
                }
                accrualStr = "8.00";
                hoursWorked = 0.0;
                dailyOvertimeHours = 0.0;
                dailyNightHours = 0.0;
            } else {
                TimeTracking checkIn = dayTrackings.stream()
                        .filter(t -> t.getType() == TrackingType.CHECK_IN)
                        .findFirst().orElse(null);
                TimeTracking checkOut = dayTrackings.stream()
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
                        if ("Absence injustifiée".equals(checkOut.getAttendanceStatus())) {
                            status = "Absence injustifiée";
                        }
                    }
                    
                    if ("Absence justifiée (Maladie)".equals(status)) {
                        payCode = "M - Maladie";
                        accrualStr = "8.00";
                    } else if (hoursWorked > 0) {
                        accrualStr = String.format(java.util.Locale.US, "%.2f", hoursWorked);
                    }
                    
                    // Resolve and assign daily location
                    dailyLocation = getGeocodedLocation(profile, checkIn.getLatitude(), checkIn.getLongitude());
                } else {
                    java.time.DayOfWeek dow = finalCurrent.getDayOfWeek();
                    if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
                        status = "Week-end";
                        accrualStr = "—";
                    } else {
                        status = "Absent";
                        accrualStr = "0.00";
                    }
                }
            }

            if (hoursWorked > 0 || daily.isMissingCheckout()) {
                totalHours += daily.getTotalHours();
                overtimeHours += daily.getOvertimeHours();
                daysWorked++;
            }

            details.add(DailyAttendanceDetail.builder()
                    .date(finalCurrent)
                    .checkInTime(checkInTime)
                    .checkOutTime(checkOutTime)
                    .hoursWorked(hoursWorked)
                    .overtimeHours(dailyOvertimeHours)
                    .nightHours(dailyNightHours)
                    .status(status)
                    .trackingId(trackingId)
                    .location(dailyLocation)
                    .payCode(payCode)
                    .accrual(accrualStr)
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
        } else if (tracking.getType() == TrackingType.CHECK_OUT) {
            java.time.LocalTime checkOutTime = tracking.getTimestamp().toLocalTime();
            java.time.LocalTime plannedEnd = java.time.LocalTime.of(SHIFT_END_HOUR, 0);
            java.time.LocalTime toleranceEnd = plannedEnd.minusMinutes(5); // 15:55
            
            if (checkOutTime.isBefore(toleranceEnd)) {
                tracking.setAttendanceStatus("Absence injustifiée");
            } else {
                tracking.setAttendanceStatus("Normal");
            }
        }
        
        // Populate geocoded location
        tracking.setLocation(getGeocodedLocation(employee, tracking.getLatitude(), tracking.getLongitude()));
        
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

    private String getGeocodedLocation(EmployeeProfile profile, Double latitude, Double longitude) {
        if (profile == null) {
            return "Poitiers, France";
        }
        
        String workModel = profile.getWorkModel();
        if ("OFFICE".equalsIgnoreCase(workModel)) {
            return "Poitiers, France";
        }
        
        if (latitude == null || longitude == null) {
            return "Poitiers, France";
        }
        
        // Reverse geocoding for WFH or others
        try {
            // Standard Nominatim OpenStreetMap URL
            String urlString = String.format(
                java.util.Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%.6f&lon=%.6f",
                latitude, longitude
            );
            
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Set User-Agent as required by Nominatim Policy
            conn.setRequestProperty("User-Agent", "NexusERP-TimeTrackingApp/1.0");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String json = response.toString();
                // Simple JSON extraction to avoid heavy dependencies
                String city = extractJsonField(json, "city");
                if (city == null || city.isEmpty()) {
                    city = extractJsonField(json, "town");
                }
                if (city == null || city.isEmpty()) {
                    city = extractJsonField(json, "village");
                }
                if (city == null || city.isEmpty()) {
                    city = extractJsonField(json, "suburb");
                }
                if (city == null || city.isEmpty()) {
                    city = "Inconnu";
                }
                
                String country = extractJsonField(json, "country");
                if (country == null || country.isEmpty()) {
                    country = "France";
                }
                
                return city + ", " + country;
            }
        } catch (Exception e) {
            // Fallback simulated geocoding
            System.err.println("Nominatim reverse geocoding failed, using simulation: " + e.getMessage());
        }
        
        // Fallback simulated geocoding based on coordinates
        if (Math.abs(latitude - 48.8566) < 1.0 && Math.abs(longitude - 2.3522) < 1.0) {
            return "Paris, France";
        } else if (Math.abs(latitude - 43.6047) < 1.0 && Math.abs(longitude - 1.4442) < 1.0) {
            return "Toulouse, France";
        } else if (Math.abs(latitude - 46.5802) < 1.0 && Math.abs(longitude - 0.3404) < 1.0) {
            return "Poitiers, France";
        }
        
        return "Poitiers, France";
    }
    
    private String extractJsonField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Permet à un manager de corriger ou de forcer les pointages et statuts d'une journée.
     */
    @Transactional
    public void correctTimeTracking(com.dyxia.nexuserp.dto.TimeCorrectionRequest request) {
        Long employeeId = request.getEmployeeId();
        LocalDate date = request.getDate();

        EmployeeProfile employee = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + employeeId));

        // 1. Supprimer les pointages existants pour ce jour
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<TimeTracking> existingTrackings = timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                employeeId, startOfDay, endOfDay
        );
        timeTrackingRepository.deleteAll(existingTrackings);

        // 2. Traiter le Pay Code (congés / maladie)
        List<LeaveRequest> existingLeaves = leaveRequestRepository.findValidatedLeavesForRange(employeeId, date, date);
        leaveRequestRepository.deleteAll(existingLeaves);

        String payCode = request.getPayCode();
        if (payCode != null && !payCode.trim().isEmpty() && !payCode.equals("—")) {
            LeaveType leaveType = null;
            if (payCode.contains("Maladie") || payCode.contains("M ")) {
                leaveType = LeaveType.SICK;
            } else if (payCode.contains("Congé") || payCode.contains("C ")) {
                leaveType = LeaveType.ANNUAL;
            }

            if (leaveType != null) {
                LeaveRequest leave = LeaveRequest.builder()
                        .startDate(date)
                        .endDate(date)
                        .type(leaveType)
                        .status(LeaveStatus.VALIDATED_N1)
                        .reason("Correction par le manager")
                        .employeeProfile(employee)
                        .build();
                leaveRequestRepository.save(leave);
            }
        }

        // 3. Traiter les horaires IN / OUT
        String checkInTime = request.getCheckInTime();
        String checkOutTime = request.getCheckOutTime();

        if (checkInTime != null && !checkInTime.trim().isEmpty() && !checkInTime.equals("—")) {
            java.time.LocalTime inLocal = parseTime(checkInTime);
            if (inLocal != null) {
                TimeTracking inTracking = TimeTracking.builder()
                        .timestamp(date.atTime(inLocal))
                        .type(TrackingType.CHECK_IN)
                        .attendanceStatus("Normal")
                        .employeeProfile(employee)
                        .build();
                timeTrackingRepository.save(inTracking);
            }
        }

        if (checkOutTime != null && !checkOutTime.trim().isEmpty() && !checkOutTime.equals("—")) {
            java.time.LocalTime outLocal = parseTime(checkOutTime);
            if (outLocal != null) {
                TimeTracking outTracking = TimeTracking.builder()
                        .timestamp(date.atTime(outLocal))
                        .type(TrackingType.CHECK_OUT)
                        .attendanceStatus("Normal")
                        .employeeProfile(employee)
                        .build();
                timeTrackingRepository.save(outTracking);
            }
        }
    }

    private java.time.LocalTime parseTime(String timeStr) {
        if (timeStr == null) return null;
        timeStr = timeStr.trim().toUpperCase();
        try {
            if (timeStr.contains("AM") || timeStr.contains("PM")) {
                java.time.format.DateTimeFormatter ampmFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
                if (!timeStr.contains(" ") && timeStr.length() >= 7) {
                    timeStr = timeStr.substring(0, 5) + " " + timeStr.substring(5);
                }
                return java.time.LocalTime.parse(timeStr, ampmFormatter);
            } else {
                java.time.format.DateTimeFormatter format24 = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                return java.time.LocalTime.parse(timeStr, format24);
            }
        } catch (Exception e) {
            System.err.println("Échec du parsing de l'heure : " + timeStr + " - " + e.getMessage());
            return null;
        }
    }
}
