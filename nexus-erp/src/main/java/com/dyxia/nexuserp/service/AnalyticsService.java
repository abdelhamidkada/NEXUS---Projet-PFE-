package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.AnalyticsKpiResponse;
import com.dyxia.nexuserp.model.*;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EmployeeProfileRepository employeeProfileRepository;

    @Transactional(readOnly = true)
    public AnalyticsKpiResponse calculateKpis() {
        List<EmployeeProfile> profiles = employeeProfileRepository.findAll();

        int totalEmployees = profiles.size();
        if (totalEmployees == 0) {
            return AnalyticsKpiResponse.builder()
                    .globalAttritionRisk(0.0)
                    .averageAttendanceRate(100.0)
                    .trainingAlertsCount(0)
                    .build();
        }

        int trainingAlertsCount = 0;
        int onLeaveTodayCount = 0;
        double totalAttritionRisk = 0.0;
        LocalDate today = LocalDate.now();

        for (EmployeeProfile profile : profiles) {
            // Heuristic 1: Training alerts (any skill <= 2)
            boolean hasLowSkill = false;
            if (profile.getSkills() != null) {
                for (EmployeeSkill s : profile.getSkills()) {
                    if (s.getProficiencyLevel() != null && s.getProficiencyLevel() <= 2) {
                        hasLowSkill = true;
                        break;
                    }
                }
            }
            if (hasLowSkill) {
                trainingAlertsCount++;
            }

            // Heuristic 2: Attendance rate (on leave today check)
            boolean isOnLeave = false;
            if (profile.getLeaveRequests() != null) {
                for (LeaveRequest lr : profile.getLeaveRequests()) {
                    if ((lr.getStatus() == LeaveStatus.VALIDATED_N1 || lr.getStatus() == LeaveStatus.PROCESSED_HR)
                            && (lr.getStartDate().isBefore(today) || lr.getStartDate().isEqual(today))
                            && (lr.getEndDate().isAfter(today) || lr.getEndDate().isEqual(today))) {
                        isOnLeave = true;
                        break;
                    }
                }
            }
            if (isOnLeave) {
                onLeaveTodayCount++;
            }

            // Heuristic 3: Attrition risk calculation
            double profileRisk = 5.0; // Base risk 5%
            
            // Add +2% for each exceptional or unpaid leave request
            if (profile.getLeaveRequests() != null) {
                for (LeaveRequest lr : profile.getLeaveRequests()) {
                    if (lr.getType() == LeaveType.UNPAID || lr.getType() == LeaveType.EXCEPTIONAL) {
                        profileRisk += 2.0;
                    }
                }
            }
            
            // Add +3% if they have very low skills (<= 2)
            if (hasLowSkill) {
                profileRisk += 3.0;
            }

            // Attrition risk capped at 100%
            if (profileRisk > 100.0) {
                profileRisk = 100.0;
            }
            
            totalAttritionRisk += profileRisk;
        }

        double averageAttendanceRate = ((double) (totalEmployees - onLeaveTodayCount) / totalEmployees) * 100.0;
        double globalAttritionRisk = totalAttritionRisk / totalEmployees;

        return AnalyticsKpiResponse.builder()
                .globalAttritionRisk(globalAttritionRisk)
                .averageAttendanceRate(averageAttendanceRate)
                .trainingAlertsCount(trainingAlertsCount)
                .build();
    }
}
