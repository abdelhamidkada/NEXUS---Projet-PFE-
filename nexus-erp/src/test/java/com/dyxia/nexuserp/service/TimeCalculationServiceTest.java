package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.DailyTimeReport;
import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.model.TrackingType;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.TimeTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TimeCalculationServiceTest {

    private TimeTrackingRepository timeTrackingRepository;
    private EmployeeProfileRepository employeeProfileRepository;
    private com.dyxia.nexuserp.repository.LeaveRequestRepository leaveRequestRepository;
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    void setUp() {
        timeTrackingRepository = Mockito.mock(TimeTrackingRepository.class);
        employeeProfileRepository = Mockito.mock(EmployeeProfileRepository.class);
        leaveRequestRepository = Mockito.mock(com.dyxia.nexuserp.repository.LeaveRequestRepository.class);
        timeCalculationService = new TimeCalculationService(timeTrackingRepository, employeeProfileRepository, leaveRequestRepository);
    }

    @Test
    void testGetMonthlyCycleRange() {
        // Date on or after 16th (e.g. 2026-06-18)
        LocalDate dateAfter16 = LocalDate.of(2026, 6, 18);
        LocalDate[] rangeAfter16 = timeCalculationService.getMonthlyCycleRange(dateAfter16);
        assertEquals(LocalDate.of(2026, 6, 16), rangeAfter16[0]);
        assertEquals(LocalDate.of(2026, 7, 15), rangeAfter16[1]);

        // Date before 16th (e.g. 2026-06-11)
        LocalDate dateBefore16 = LocalDate.of(2026, 6, 11);
        LocalDate[] rangeBefore16 = timeCalculationService.getMonthlyCycleRange(dateBefore16);
        assertEquals(LocalDate.of(2026, 5, 16), rangeBefore16[0]);
        assertEquals(LocalDate.of(2026, 6, 15), rangeBefore16[1]);
    }

    @Test
    void testCalculateDailyTimeStandardShift() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 11);

        // Checked-in at 07:30, Checked-out at 17:00
        TimeTracking in = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 7, 30))
                .type(TrackingType.CHECK_IN)
                .build();
        TimeTracking out = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 17, 0))
                .type(TrackingType.CHECK_OUT)
                .build();

        Mockito.when(timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Arrays.asList(in, out));

        DailyTimeReport report = timeCalculationService.calculateDailyTime(employeeId, date);

        // Regular: 08:00 - 16:00 (8h)
        // Overtime: 16:00 - 17:00 (1h)
        // Total: 9h
        assertEquals(9.0, report.getTotalHours());
        assertEquals(1.0, report.getOvertimeHours());
        assertFalse(report.isMissingCheckout());
    }

    @Test
    void testCalculateDailyTimeOvertimeStrictLimit() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 11);

        // Checked-in at 08:00, Checked-out at 21:00 (overtime stops at 20:00)
        TimeTracking in = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 8, 0))
                .type(TrackingType.CHECK_IN)
                .build();
        TimeTracking out = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 21, 0))
                .type(TrackingType.CHECK_OUT)
                .build();

        Mockito.when(timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Arrays.asList(in, out));

        DailyTimeReport report = timeCalculationService.calculateDailyTime(employeeId, date);

        // Regular: 08:00 - 16:00 (8h)
        // Overtime: 16:00 - 20:00 (4h max)
        // Total: 12h
        assertEquals(12.0, report.getTotalHours());
        assertEquals(4.0, report.getOvertimeHours());
    }

    @Test
    void testRecordTimeTrackingWithinTolerance() {
        Long employeeId = 1L;
        com.dyxia.nexuserp.model.EmployeeProfile employee = com.dyxia.nexuserp.model.EmployeeProfile.builder().id(employeeId).build();
        Mockito.when(employeeProfileRepository.findById(employeeId)).thenReturn(java.util.Optional.of(employee));
        Mockito.when(timeTrackingRepository.save(any(TimeTracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 08:05 CHECK_IN
        TimeTracking tracking = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 8, 5))
                .type(TrackingType.CHECK_IN)
                .build();

        TimeTracking recorded = timeCalculationService.recordTimeTracking(employeeId, tracking);
        assertEquals("Normal", recorded.getAttendanceStatus());
    }

    @Test
    void testRecordTimeTrackingLate() {
        Long employeeId = 1L;
        com.dyxia.nexuserp.model.EmployeeProfile employee = com.dyxia.nexuserp.model.EmployeeProfile.builder().id(employeeId).build();
        Mockito.when(employeeProfileRepository.findById(employeeId)).thenReturn(java.util.Optional.of(employee));
        Mockito.when(timeTrackingRepository.save(any(TimeTracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 08:06 CHECK_IN
        TimeTracking tracking = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 8, 6))
                .type(TrackingType.CHECK_IN)
                .build();

        TimeTracking recorded = timeCalculationService.recordTimeTracking(employeeId, tracking);
        assertEquals("Absence injustifiée", recorded.getAttendanceStatus());
    }

    @Test
    void testOverrideLatePunchIn() {
        java.util.UUID trackingId = java.util.UUID.randomUUID();
        TimeTracking tracking = TimeTracking.builder()
                .id(trackingId)
                .attendanceStatus("Absence injustifiée")
                .build();

        Mockito.when(timeTrackingRepository.findById(trackingId)).thenReturn(java.util.Optional.of(tracking));
        Mockito.when(timeTrackingRepository.save(any(TimeTracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeTracking updated = timeCalculationService.overrideLatePunchIn(trackingId);
        assertEquals("Absence justifiée (Maladie)", updated.getAttendanceStatus());
    }
}
