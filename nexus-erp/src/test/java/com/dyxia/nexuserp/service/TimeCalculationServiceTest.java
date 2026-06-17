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
    void testGetMonthlyCycleRangeBoundaries() {
        // Event on the 15th: e.g. 2026-06-15 -> falls in previous cycle [2026-05-16, 2026-06-15]
        LocalDate date15 = LocalDate.of(2026, 6, 15);
        LocalDate[] range15 = timeCalculationService.getMonthlyCycleRange(date15);
        System.out.println("SIMULATION DATE BOUNDARY: Event on " + date15 + " => Cycle Range = [" + range15[0] + " to " + range15[1] + "]");
        assertEquals(LocalDate.of(2026, 5, 16), range15[0]);
        assertEquals(LocalDate.of(2026, 6, 15), range15[1]);

        // Event on the 16th: e.g. 2026-06-16 -> falls in new cycle [2026-06-16, 2026-07-15]
        LocalDate date16 = LocalDate.of(2026, 6, 16);
        LocalDate[] range16 = timeCalculationService.getMonthlyCycleRange(date16);
        System.out.println("SIMULATION DATE BOUNDARY: Event on " + date16 + " => Cycle Range = [" + range16[0] + " to " + range16[1] + "]");
        assertEquals(LocalDate.of(2026, 6, 16), range16[0]);
        assertEquals(LocalDate.of(2026, 7, 15), range16[1]);
    }

    @Test
    void testRecordTimeTrackingWithinTolerance() {
        Long employeeId = 1L;
        com.dyxia.nexuserp.model.EmployeeProfile employee = com.dyxia.nexuserp.model.EmployeeProfile.builder().id(employeeId).build();
        Mockito.when(employeeProfileRepository.findById(employeeId)).thenReturn(java.util.Optional.of(employee));
        Mockito.when(timeTrackingRepository.save(any(TimeTracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 08:04 CHECK_IN
        TimeTracking tracking04 = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 8, 4))
                .type(TrackingType.CHECK_IN)
                .build();
        TimeTracking recorded04 = timeCalculationService.recordTimeTracking(employeeId, tracking04);
        System.out.println("SIMULATION PUNCH-IN TOLERANCE: Punch-in at 08:04 => Status = " + recorded04.getAttendanceStatus());
        assertEquals("Normal", recorded04.getAttendanceStatus());

        // 08:05 CHECK_IN
        TimeTracking tracking05 = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 8, 5))
                .type(TrackingType.CHECK_IN)
                .build();
        TimeTracking recorded05 = timeCalculationService.recordTimeTracking(employeeId, tracking05);
        System.out.println("SIMULATION PUNCH-IN TOLERANCE: Punch-in at 08:05 => Status = " + recorded05.getAttendanceStatus());
        assertEquals("Normal", recorded05.getAttendanceStatus());
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
        System.out.println("SIMULATION PUNCH-IN TOLERANCE: Punch-in at 08:06 => Status = " + recorded.getAttendanceStatus());
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
        System.out.println("SIMULATION OVERRIDE: Original = Absence injustifiée => Overridden = " + updated.getAttendanceStatus());
        assertEquals("Absence justifiée (Maladie)", updated.getAttendanceStatus());
    }

    @Test
    void testCalculateNightHoursDayShift() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 11, 8, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 11, 17, 0);
        java.time.Duration duration = timeCalculationService.calculateNightHours(checkIn, checkOut);
        assertEquals(java.time.Duration.ZERO, duration);
    }

    @Test
    void testCalculateNightHoursCrossingMidnight() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 11, 22, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 12, 4, 0);
        java.time.Duration duration = timeCalculationService.calculateNightHours(checkIn, checkOut);
        // From 22:00 to 04:00 = 6 hours
        assertEquals(java.time.Duration.ofHours(6), duration);
    }

    @Test
    void testCalculateNightHoursShiftStartsBefore21EndsInside() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 11, 18, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 11, 23, 0);
        java.time.Duration duration = timeCalculationService.calculateNightHours(checkIn, checkOut);
        // From 21:00 to 23:00 = 2 hours
        assertEquals(java.time.Duration.ofHours(2), duration);
    }

    @Test
    void testCalculateNightHoursShiftStartsInsideEndsAfter06() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 12, 2, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 12, 10, 0);
        java.time.Duration duration = timeCalculationService.calculateNightHours(checkIn, checkOut);
        // From 02:00 to 06:00 = 4 hours
        assertEquals(java.time.Duration.ofHours(4), duration);
    }

    @Test
    void testCalculateNightHoursLongShift() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 11, 18, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 12, 8, 0);
        java.time.Duration duration = timeCalculationService.calculateNightHours(checkIn, checkOut);
        // From D1 21:00 to D2 06:00 = 9 hours
        assertEquals(java.time.Duration.ofHours(9), duration);
    }

    @Test
    void testCalculateDailyTimeWithNightHours() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 11);

        TimeTracking in = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 21, 0))
                .type(TrackingType.CHECK_IN)
                .build();
        TimeTracking out = TimeTracking.builder()
                .timestamp(LocalDateTime.of(2026, 6, 11, 23, 30))
                .type(TrackingType.CHECK_OUT)
                .build();

        Mockito.when(timeTrackingRepository.findByEmployeeProfileIdAndTimestampBetween(
                eq(employeeId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Arrays.asList(in, out));

        DailyTimeReport report = timeCalculationService.calculateDailyTime(employeeId, date);

        assertEquals(2.5, report.getTotalHours());
        assertEquals(0.0, report.getOvertimeHours());
        assertEquals(2.5, report.getNightHours());
    }
}
