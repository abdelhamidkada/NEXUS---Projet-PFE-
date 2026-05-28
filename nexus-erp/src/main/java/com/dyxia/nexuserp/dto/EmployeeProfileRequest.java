package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the payload to create or update an Employee Profile.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileRequest {
    private Long userId;
    private String jobTitle;
    private String department;
    private String rib;
}
