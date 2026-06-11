package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.LeaveRequestResponse;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.LeaveStatus;
import java.util.List;
import java.util.UUID;

/**
 * Service gérant le cycle de vie des demandes de congés.
 */
public interface LeaveRequestService {

    /**
     * Soumet une demande de congé pour un employé spécifié par son ID (Long).
     * Force le statut initial à PENDING.
     *
     * @param employeeId L'ID du profil de l'employé.
     * @param request    La demande de congé à soumettre.
     * @return La demande de congé sauvegardée.
     */
    LeaveRequest submitLeaveRequest(Long employeeId, LeaveRequest request);

    /**
     * Soumet une demande de congé pour un employé spécifié par un UUID.
     * Force le statut initial à PENDING.
     *
     * @param employeeId L'UUID de l'employé.
     * @param request    La demande de congé à soumettre.
     * @return La demande de congé sauvegardée.
     */
    LeaveRequest submitLeaveRequest(UUID employeeId, LeaveRequest request);

    /**
     * Modifie le statut d'une demande de congé selon une machine à état stricte.
     *
     * @param requestId L'ID de la demande de congé.
     * @param newStatus Le nouveau statut demandé.
     * @param comment   Le commentaire du manager ou des RH.
     * @param userRole  Le rôle de l'utilisateur effectuant l'action (MANAGER, HR_ADMIN, etc.).
     * @return La demande de congé mise à jour.
     */
    LeaveRequest changeLeaveStatus(UUID requestId, LeaveStatus newStatus, String comment, String userRole);

    /**
     * Récupère toutes les demandes de congés de la base de données mappées en DTOs.
     *
     * @return La liste de toutes les demandes de congés.
     */
    List<LeaveRequestResponse> getAllLeaveRequests();
}

