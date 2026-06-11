package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.LeaveRequestResponse;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.LeaveStatus;
import com.dyxia.nexuserp.service.LeaveRequestService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des demandes de congés.
 */
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * Endpoint POST pour soumettre une nouvelle demande de congé.
     * 
     * @param employeeId L'identifiant de l'employé (passé en paramètre de requête).
     * @param request    Les détails de la demande de congé.
     * @return La demande de congé créée avec le statut 201 Created.
     */
    @PostMapping
    public ResponseEntity<LeaveRequest> submitLeaveRequest(
            @RequestParam Long employeeId,
            @RequestBody LeaveRequest request) {
        LeaveRequest submitted = leaveRequestService.submitLeaveRequest(employeeId, request);
        return new ResponseEntity<>(submitted, HttpStatus.CREATED);
    }

    /**
     * Endpoint GET pour récupérer toutes les demandes de congés.
     * 
     * @return La liste de toutes les demandes de congés sous forme de LeaveRequestResponse.
     */
    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    /**
     * Endpoint PATCH pour modifier le statut d'une demande de congé.
     * 
     * @param id             L'identifiant UUID de la demande de congé.
     * @param updateRequest Le corps contenant le nouveau statut, le commentaire et le rôle de l'utilisateur.
     * @return La demande de congé mise à jour avec le statut 200 OK.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequest> changeLeaveStatus(
            @PathVariable UUID id,
            @RequestBody LeaveStatusUpdateRequest updateRequest) {
        LeaveRequest updated = leaveRequestService.changeLeaveStatus(
                id,
                updateRequest.getStatus(),
                updateRequest.getComment(),
                updateRequest.getUserRole()
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * DTO statique interne représentant la requête de mise à jour du statut.
     */
    @Data
    public static class LeaveStatusUpdateRequest {
        private LeaveStatus status;
        private String comment;
        private String userRole;
    }
}

