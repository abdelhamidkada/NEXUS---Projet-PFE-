package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.model.TrackingType;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.LeaveRequestRepository;
import com.dyxia.nexuserp.repository.TimeTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeStatusController {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimeTrackingRepository timeTrackingRepository;

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getEmployeeStatus(@PathVariable Long id) {
        EmployeeProfile profile = employeeProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + id));

        // a) Vérifier la table LEAVE_REQUESTS : si une demande est validée pour aujourd'hui -> Retourner 'Absent (Congé)'
        List<LeaveRequest> todayLeaves = leaveRequestRepository.findValidatedLeavesForToday(id, LocalDate.now());
        if (!todayLeaves.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "Absent (Congé)"));
        }

        // b) Vérifier la table TIME_TRACKING : si l'employé a un CHECK_IN mais pas de CHECK_OUT aujourd'hui -> Retourner 'Au bureau' ou 'En télétravail' (basé sur le champ workModel)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<TimeTracking> todayTrackings = timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(id, startOfDay, endOfDay);

        boolean hasCheckIn = todayTrackings.stream().anyMatch(t -> t.getType() == TrackingType.CHECK_IN);
        boolean hasCheckOut = todayTrackings.stream().anyMatch(t -> t.getType() == TrackingType.CHECK_OUT);

        if (hasCheckIn && !hasCheckOut) {
            String workModel = profile.getWorkModel();
            if ("WFH".equalsIgnoreCase(workModel)) {
                return ResponseEntity.ok(Map.of("status", "En télétravail"));
            } else {
                return ResponseEntity.ok(Map.of("status", "Au bureau"));
            }
        }

        // c) Sinon -> Retourner 'Absent'
        return ResponseEntity.ok(Map.of("status", "Absent"));
    }
}
