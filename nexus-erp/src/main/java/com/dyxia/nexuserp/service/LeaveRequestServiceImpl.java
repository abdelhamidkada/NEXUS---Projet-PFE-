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
                
                // Imputation du solde de congés si le type est ANNUAL (Congé Annuel)
                if (leaveRequest.getType() == com.dyxia.nexuserp.model.LeaveType.ANNUAL) {
                    EmployeeProfile employee = leaveRequest.getEmployeeProfile();
                    if (employee != null) {
                        double duration = calculateWorkingDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());
                        double currentBalance = employee.getLeaveBalance() != null ? employee.getLeaveBalance() : 0.0;
                        employee.setLeaveBalance(currentBalance - duration);
                        employeeProfileRepository.save(employee);
                    }
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

    private static double calculateWorkingDays(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }
        double count = 0;
        java.time.LocalDate curr = start;
        while (!curr.isAfter(end)) {
            if (curr.getDayOfWeek() != java.time.DayOfWeek.SUNDAY && !isFrenchPublicHoliday(curr)) {
                count++;
            }
            curr = curr.plusDays(1);
        }
        return count;
    }

    private static boolean isFrenchPublicHoliday(java.time.LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Jours fériés fixes en France
        if (month == 1 && day == 1) return true;   // Jour de l'An
        if (month == 5 && day == 1) return true;   // Fête du Travail
        if (month == 5 && day == 8) return true;   // Victoire 1945
        if (month == 7 && day == 14) return true;  // Fête Nationale
        if (month == 8 && day == 15) return true;  // Assomption
        if (month == 11 && day == 1) return true;  // Toussaint
        if (month == 11 && day == 11) return true; // Armistice 1918
        if (month == 12 && day == 25) return true; // Noël

        // Jours fériés mobiles basés sur Pâques
        java.time.LocalDate easter = getEasterSunday(year);
        if (date.equals(easter.plusDays(1))) return true;  // Lundi de Pâques
        if (date.equals(easter.plusDays(39))) return true; // Ascension
        if (date.equals(easter.plusDays(50))) return true; // Lundi de Pentecôte

        return false;
    }

    private static java.time.LocalDate getEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return java.time.LocalDate.of(year, month, day);
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

