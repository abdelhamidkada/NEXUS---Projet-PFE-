package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.LeaveRequestResponse;
import com.dyxia.nexuserp.exception.InvalidLeaveTransitionException;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.LeaveRequest;
import com.dyxia.nexuserp.model.LeaveStatus;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service LeaveRequestService gérant le workflow de demande de congés.
 */
@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    /**
     * Constructeur pour l'injection des dépendances (pas de @Autowired sur les champs).
     */
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeProfileRepository employeeProfileRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    @Override
    public LeaveRequest submitLeaveRequest(Long employeeId, LeaveRequest request) {
        EmployeeProfile employee = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + employeeId));

        if (request.getType() == com.dyxia.nexuserp.model.LeaveType.SICK) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthorized = false;
            if (auth != null && auth.isAuthenticated()) {
                isAuthorized = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_HR_ADMIN") || a.getAuthority().equals("ROLE_DIRECTION"));
            }
            if (!isAuthorized) {
                throw new IllegalArgumentException("Seuls les RH et la Direction peuvent soumettre des demandes de congé maladie.");
            }
        }

        // Assurer que le statut initial est toujours forcé à PENDING
        request.setStatus(LeaveStatus.PENDING);
        request.setEmployeeProfile(employee);

        return leaveRequestRepository.save(request);
    }

    @Override
    public LeaveRequest submitLeaveRequest(UUID employeeId, LeaveRequest request) {
        try {
            // Conversion de l'UUID en Long puisque l'ID d'EmployeeProfile est de type Long
            Long id = Long.parseLong(employeeId.toString());
            return submitLeaveRequest(id, request);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'identifiant de l'employé doit correspondre au type Long utilisé par EmployeeProfile. L'UUID fourni n'est pas un nombre : " + employeeId);
        }
    }

    @Override
    public LeaveRequest changeLeaveStatus(UUID requestId, LeaveStatus newStatus, String comment, String userRole) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé non trouvée avec l'ID : " + requestId));

        LeaveStatus currentStatus = leaveRequest.getStatus();

        // Machine à état stricte
        if (currentStatus == LeaveStatus.PENDING) {
            if (newStatus == LeaveStatus.VALIDATED_N1) {
                if (!"MANAGER".equals(userRole)) {
                    throw new InvalidLeaveTransitionException("Seul un MANAGER est autorisé à valider une demande en attente (PENDING -> VALIDATED_N1).");
                }
            } else if (newStatus == LeaveStatus.REJECTED) {
                if (!"MANAGER".equals(userRole)) {
                    throw new InvalidLeaveTransitionException("Seul un MANAGER est autorisé à rejeter une demande en attente (PENDING -> REJECTED).");
                }
            } else {
                throw new InvalidLeaveTransitionException("Transition d'état non autorisée : PENDING vers " + newStatus);
            }
        } else if (currentStatus == LeaveStatus.VALIDATED_N1) {
            if (newStatus == LeaveStatus.PROCESSED_HR) {
                if (!"HR_ADMIN".equals(userRole)) {
                    throw new InvalidLeaveTransitionException("Seul un administrateur RH (HR_ADMIN) est autorisé à traiter une demande déjà validée N1 (VALIDATED_N1 -> PROCESSED_HR).");
                }
            } else {
                throw new InvalidLeaveTransitionException("Transition d'état non autorisée : VALIDATED_N1 vers " + newStatus);
            }
        } else {
            throw new InvalidLeaveTransitionException("Aucune transition de statut n'est autorisée depuis l'état actuel : " + currentStatus);
        }

        // Mettre à jour le statut et sauvegarder le commentaire si présent
        leaveRequest.setStatus(newStatus);
        if (comment != null && !comment.trim().isEmpty()) {
            leaveRequest.setManagerComment(comment);
        }

        return leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public List<LeaveRequestResponse> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(request -> {
                    EmployeeProfile profile = request.getEmployeeProfile();
                    String employeeName = "Collaborateur";
                    String employeeEmail = "";
                    if (profile != null && profile.getUser() != null) {
                        employeeName = profile.getUser().getFirstName() + " " + profile.getUser().getLastName();
                        employeeEmail = profile.getUser().getEmail();
                    }
                    return LeaveRequestResponse.builder()
                            .id(request.getId())
                            .startDate(request.getStartDate())
                            .endDate(request.getEndDate())
                            .type(request.getType())
                            .status(request.getStatus())
                            .reason(request.getReason())
                            .managerComment(request.getManagerComment())
                            .employeeId(profile != null ? profile.getId() : null)
                            .employeeName(employeeName)
                            .employeeEmail(employeeEmail)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

