package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.Employee;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour l'exportation des profils d'employés sous différents formats.
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeExportController {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final PdfExportService pdfExportService;

    /**
     * Endpoint GET pour exporter le profil d'un employé au format PDF.
     * 
     * @param id L'identifiant unique du profil de l'employé.
     * @return Une ResponseEntity contenant le flux de données PDF en pièce jointe.
     */
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportEmployeePdf(@PathVariable Long id) {
        EmployeeProfile profile = employeeProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + id));

        String fullName = profile.getUser() != null 
                ? profile.getUser().getFirstName() + " " + profile.getUser().getLastName() 
                : "Employé sans compte";

        Employee employee = Employee.builder()
                .fullName(fullName)
                .matricule(profile.getMatricule())
                .role(profile.getJobTitle())
                .department(profile.getDepartment())
                .build();

        byte[] pdfBytes = pdfExportService.generateEmployeePdf(employee);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee_" + profile.getMatricule() + ".pdf\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
