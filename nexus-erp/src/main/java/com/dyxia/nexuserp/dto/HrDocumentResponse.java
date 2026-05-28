package com.dyxia.nexuserp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an HR document in the 360° profile view.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HrDocumentResponse {
    private Long id;
    private String documentType;
    private String filePath;
    private LocalDateTime uploadDate;
    private Boolean isSigned;
}
