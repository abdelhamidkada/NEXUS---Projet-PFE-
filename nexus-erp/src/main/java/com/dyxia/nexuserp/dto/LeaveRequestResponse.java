package com.dyxia.nexuserp.dto;

import com.dyxia.nexuserp.model.LeaveStatus;
import com.dyxia.nexuserp.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object pour représenter une demande de congé avec les détails de l'employé associé.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestResponse {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type;
    private LeaveStatus status;
    private String reason;
    private String managerComment;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
}
