package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.MonthlyCycle;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.MonthlyCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class LeaveAccrualServiceTest {

    private MonthlyCycleRepository monthlyCycleRepository;
    private EmployeeProfileRepository employeeProfileRepository;
    private LeaveAccrualService leaveAccrualService;

    @BeforeEach
    void setUp() {
        monthlyCycleRepository = Mockito.mock(MonthlyCycleRepository.class);
        employeeProfileRepository = Mockito.mock(EmployeeProfileRepository.class);
        leaveAccrualService = new LeaveAccrualService(monthlyCycleRepository, employeeProfileRepository);
    }

    @Test
    void testProcessAccrualsNoCycles() {
        Mockito.when(monthlyCycleRepository.findByValidatedAsWorkedTrueAndProcessedForAccrualFalse())
                .thenReturn(Collections.emptyList());

        leaveAccrualService.processAccruals();

        Mockito.verify(employeeProfileRepository, Mockito.never()).save(any(EmployeeProfile.class));
        Mockito.verify(monthlyCycleRepository, Mockito.never()).save(any(MonthlyCycle.class));
    }

    @Test
    void testProcessAccrualsIncrementsBalance() {
        EmployeeProfile profile = EmployeeProfile.builder()
                .id(1L)
                .leaveBalance(10.0)
                .build();

        MonthlyCycle cycle = MonthlyCycle.builder()
                .id(100L)
                .employeeProfile(profile)
                .startDate(LocalDate.of(2026, 5, 16))
                .endDate(LocalDate.of(2026, 6, 15))
                .validatedAsWorked(true)
                .processedForAccrual(false)
                .build();

        Mockito.when(monthlyCycleRepository.findByValidatedAsWorkedTrueAndProcessedForAccrualFalse())
                .thenReturn(Arrays.asList(cycle));

        leaveAccrualService.processAccruals();

        assertEquals(12.5, profile.getLeaveBalance());
        assertTrue(cycle.isProcessedForAccrual());

        Mockito.verify(employeeProfileRepository, Mockito.times(1)).save(profile);
        Mockito.verify(monthlyCycleRepository, Mockito.times(1)).save(cycle);
    }

    @Test
    void testProcessAccrualsHandlesNullInitialBalance() {
        EmployeeProfile profile = EmployeeProfile.builder()
                .id(2L)
                .leaveBalance(null)
                .build();

        MonthlyCycle cycle = MonthlyCycle.builder()
                .id(101L)
                .employeeProfile(profile)
                .startDate(LocalDate.of(2026, 5, 16))
                .endDate(LocalDate.of(2026, 6, 15))
                .validatedAsWorked(true)
                .processedForAccrual(false)
                .build();

        Mockito.when(monthlyCycleRepository.findByValidatedAsWorkedTrueAndProcessedForAccrualFalse())
                .thenReturn(Arrays.asList(cycle));

        leaveAccrualService.processAccruals();

        assertEquals(2.5, profile.getLeaveBalance());
        assertTrue(cycle.isProcessedForAccrual());

        Mockito.verify(employeeProfileRepository, Mockito.times(1)).save(profile);
        Mockito.verify(monthlyCycleRepository, Mockito.times(1)).save(cycle);
    }

    @Test
    void testCreateAndValidateTestCycleSuccess() {
        EmployeeProfile profile = EmployeeProfile.builder()
                .id(3L)
                .build();

        Mockito.when(employeeProfileRepository.findById(3L)).thenReturn(Optional.of(profile));
        Mockito.when(monthlyCycleRepository.save(any(MonthlyCycle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate start = LocalDate.of(2026, 5, 16);
        LocalDate end = LocalDate.of(2026, 6, 15);
        MonthlyCycle created = leaveAccrualService.createAndValidateTestCycle(3L, start, end);

        assertNotNull(created);
        assertEquals(profile, created.getEmployeeProfile());
        assertEquals(start, created.getStartDate());
        assertEquals(end, created.getEndDate());
        assertTrue(created.isValidatedAsWorked());
        assertFalse(created.isProcessedForAccrual());

        Mockito.verify(monthlyCycleRepository, Mockito.times(1)).save(any(MonthlyCycle.class));
    }

    @Test
    void testCreateAndValidateTestCycleEmployeeNotFound() {
        Mockito.when(employeeProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            leaveAccrualService.createAndValidateTestCycle(99L, LocalDate.now(), LocalDate.now())
        );
    }
}
