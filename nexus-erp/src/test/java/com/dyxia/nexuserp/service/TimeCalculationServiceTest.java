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
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    void setUp() {
        timeTrackingRepository = Mockito.mock(TimeTrackingRepository.class);
        employeeProfileRepository = Mockito.mock(EmployeeProfileRepository.class);
        timeCalculationService = new TimeCalculationService(timeTrackingRepository, employeeProfileRepository);
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
}
