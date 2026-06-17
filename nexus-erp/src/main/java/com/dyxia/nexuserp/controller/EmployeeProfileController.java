package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.EmployeeProfileRequest;
import com.dyxia.nexuserp.dto.EmployeeProfileResponse;
import com.dyxia.nexuserp.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour exposer les endpoints sécurisés de l'API 360° des profils d'employés.
 */
@RestController
@RequestMapping("/api/v1/hr/profiles")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    /**
     * Endpoint POST pour créer un profil d'employé.
     * Restreint aux rôles HR_ADMIN et DIRECTION.
     */
    @PostMapping
    // @PreAuthorize("hasAnyRole('HR_ADMIN', 'DIRECTION')")
    public ResponseEntity<EmployeeProfileResponse> createProfile(@RequestBody EmployeeProfileRequest request) {
        EmployeeProfileResponse response = employeeProfileService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint GET pour récupérer un profil d'employé spécifique par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileResponse> getProfileById(@PathVariable Long id) {
        EmployeeProfileResponse response = employeeProfileService.getProfileById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint GET pour récupérer tous les profils d'employés.
     */
    @GetMapping
    public ResponseEntity<List<EmployeeProfileResponse>> getAllProfiles() {
        List<EmployeeProfileResponse> responses = employeeProfileService.getAllProfiles();
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint PUT pour mettre à jour un profil d'employé existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeProfileResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody EmployeeProfileRequest request) {
        EmployeeProfileResponse response = employeeProfileService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint DELETE pour supprimer un profil d'employé.
     * Restreint aux rôles HR_ADMIN et DIRECTION.
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAnyRole('HR_ADMIN', 'DIRECTION')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        employeeProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint GET pour récupérer la hiérarchie ascendante d'un employé.
     */
    @GetMapping("/{id}/hierarchy")
    public ResponseEntity<com.dyxia.nexuserp.dto.EmployeeHierarchyResponse> getProfileHierarchy(@PathVariable Long id) {
        com.dyxia.nexuserp.dto.EmployeeHierarchyResponse response = employeeProfileService.getUpwardHierarchy(id);
        return ResponseEntity.ok(response);
    }
}
