package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.*;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.model.Role;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier des profils d'employés (CRUD et transformations DTO).
 * Implémente une ségrégation des données Publique [E] vs Privée [P] via l'inspection
 * du contexte de sécurité Spring Security.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeProfileService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final UserRepository userRepository;

    /**
     * Crée un nouveau profil d'employé à partir des données fournies.
     *
     * @param request DTO contenant les informations du profil à créer.
     * @return DTO représentant le profil créé avec les détails 360°.
     */
    @Transactional
    public EmployeeProfileResponse createProfile(EmployeeProfileRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("L'identifiant de l'utilisateur est obligatoire.");
        }

        if (employeeProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalStateException("Un profil d'employé existe déjà pour cet utilisateur.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + request.getUserId()));

        String matricule = request.getMatricule();
        if (matricule == null || matricule.trim().isEmpty()) {
            long count = employeeProfileRepository.count();
            matricule = String.format("E%04d", count + 1);
        }

        EmployeeProfile profile = EmployeeProfile.builder()
                .user(user)
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .matricule(matricule)
                .rib(request.getRib())
                .cin(request.getCin())
                .adresse(request.getAdresse())
                .contact(request.getContact())
                .typeContrat(request.getTypeContrat())
                .dateDebutContrat(request.getDateDebutContrat())
                .dureeContrat(request.getDureeContrat())
                .hierarchie(request.getHierarchieId() != null ? userRepository.findById(request.getHierarchieId()).orElse(null) : null)
                .photoUrl(request.getPhotoUrl())
                .signatureNumerique(request.getSignatureNumerique())
                .build();

        EmployeeProfile savedProfile = employeeProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    /**
     * Récupère un profil d'employé par son identifiant unique.
     *
     * @param id Identifiant unique du profil.
     * @return DTO représentant le profil de l'employé.
     */
    public EmployeeProfileResponse getProfileById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + id));
        return mapToResponse(profile);
    }

    /**
     * Récupère tous les profils d'employés existants.
     * Utilise une requête optimisée pour charger les utilisateurs associés d'un seul coup.
     *
     * @return Liste de tous les profils d'employés.
     */
    public List<EmployeeProfileResponse> getAllProfiles() {
        return employeeProfileRepository.findAllWithUser().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un profil d'employé existant.
     *
     * @param id Identifiant du profil à modifier.
     * @param request DTO contenant les nouvelles données.
     * @return DTO représentant le profil mis à jour.
     */
    @Transactional
    public EmployeeProfileResponse updateProfile(Long id, EmployeeProfileRequest request) {
        EmployeeProfile profile = employeeProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + id));

        if (request.getUserId() != null && !request.getUserId().equals(profile.getUser().getId())) {
            if (employeeProfileRepository.existsByUserId(request.getUserId())) {
                throw new IllegalStateException("Un profil d'employé existe déjà pour cet utilisateur.");
            }
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + request.getUserId()));
            profile.setUser(user);
        }

        profile.setJobTitle(request.getJobTitle());
        profile.setDepartment(request.getDepartment());

        if (request.getMatricule() != null && !request.getMatricule().trim().isEmpty()) {
            profile.setMatricule(request.getMatricule());
        }

        if (request.getRib() != null) {
            profile.setRib(request.getRib());
        }
        profile.setCin(request.getCin());
        profile.setAdresse(request.getAdresse());
        profile.setContact(request.getContact());
        profile.setTypeContrat(request.getTypeContrat());
        profile.setDateDebutContrat(request.getDateDebutContrat());
        profile.setDureeContrat(request.getDureeContrat());
        if (request.getHierarchieId() != null) {
            profile.setHierarchie(userRepository.findById(request.getHierarchieId()).orElse(null));
        } else {
            profile.setHierarchie(null);
        }
        profile.setPhotoUrl(request.getPhotoUrl());
        profile.setSignatureNumerique(request.getSignatureNumerique());

        EmployeeProfile savedProfile = employeeProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    /**
     * Supprime un profil d'employé de la base de données.
     *
     * @param id Identifiant du profil à supprimer.
     */
    @Transactional
    public void deleteProfile(Long id) {
        if (!employeeProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profil employé non trouvé avec l'ID : " + id);
        }
        employeeProfileRepository.deleteById(id);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mapping & Security Logic
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Convertit une entité {@link EmployeeProfile} en DTO {@link EmployeeProfileResponse}.
     * Applique la politique de ségrégation des données [E]/[P] :
     * Les champs [P] sont masqués (null) sauf si le requérant est le propriétaire
     * du profil, ou possède le rôle HR_ADMIN ou DIRECTION.
     */
    private EmployeeProfileResponse mapToResponse(EmployeeProfile profile) {
        if (profile == null) return null;

        // ── 1. Résolution du droit d'accès aux données privées [P] ───────────
        boolean canSeePrivateData = resolvePrivateDataAccess(profile);

        // ── 2. Mapping des compétences ────────────────────────────────────────
        Set<EmployeeSkillResponse> skills = null;
        if (profile.getSkills() != null) {
            skills = profile.getSkills().stream()
                    .map(es -> EmployeeSkillResponse.builder()
                            .skillId(es.getSkill().getId())
                            .name(es.getSkill().getName())
                            .category(es.getSkill().getCategory())
                            .proficiencyLevel(es.getProficiencyLevel())
                            .build())
                    .collect(Collectors.toSet());
        }

        // ── 3. Mapping des documents ──────────────────────────────────────────
        Set<HrDocumentResponse> documents = null;
        if (profile.getDocuments() != null) {
            documents = profile.getDocuments().stream()
                    .map(doc -> HrDocumentResponse.builder()
                            .id(doc.getId())
                            .documentType(doc.getDocumentType().name())
                            .filePath(doc.getFilePath())
                            .uploadDate(doc.getUploadDate())
                            .isSigned(doc.getIsSigned())
                            .build())
                    .collect(Collectors.toSet());
        }

        // ── 4. Résolution du manager (données publiques) ──────────────────────
        String managerName = null;
        String managerRole = null;
        String managerMatricule = null;

        if (profile.getHierarchie() != null) {
            User manager = profile.getHierarchie();
            managerName = manager.getFirstName() + " " + manager.getLastName();
            managerRole = manager.getRoles() != null ? manager.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse(null) : null;
            EmployeeProfile managerProfile = employeeProfileRepository.findByUser(manager).orElse(null);
            if (managerProfile != null) {
                managerMatricule = managerProfile.getMatricule();
            }
        }

        // ── 5. Calcul de l'ancienneté (timeInJob) ────────────────────────────
        String timeInJob = null;
        LocalDate referenceDate = profile.getHireDate() != null ? profile.getHireDate()
                : profile.getDateDebutContrat();
        if (referenceDate != null) {
            Period period = Period.between(referenceDate, LocalDate.now());
            int years = period.getYears();
            int months = period.getMonths();
            if (years > 0 && months > 0) {
                timeInJob = years + " an" + (years > 1 ? "s" : "") + ", " + months + " mois";
            } else if (years > 0) {
                timeInJob = years + " an" + (years > 1 ? "s" : "");
            } else if (months > 0) {
                timeInJob = months + " mois";
            } else {
                timeInJob = "Moins d'un mois";
            }
        }

        // ── 6. Construction du DTO final ──────────────────────────────────────
        return EmployeeProfileResponse.builder()
                // Champs publics [E]
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .matricule(profile.getMatricule())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .firstName(profile.getUser() != null ? profile.getUser().getFirstName() : null)
                .lastName(profile.getUser() != null ? profile.getUser().getLastName() : null)
                .photoUrl(profile.getPhotoUrl())
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .hireDate(profile.getHireDate())
                .timeInJob(timeInJob)
                .workModel(profile.getWorkModel())
                .location(profile.getLocation())
                .spokenLanguages(profile.getSpokenLanguages())
                .hierarchieId(profile.getHierarchie() != null ? profile.getHierarchie().getId() : null)
                .managerName(managerName)
                .managerRole(managerRole)
                .managerMatricule(managerMatricule)
                // Champs privés [P] — null si non autorisé
                .cin(canSeePrivateData ? profile.getCin() : null)
                .adresse(canSeePrivateData ? profile.getAdresse() : null)
                .contact(canSeePrivateData ? profile.getContact() : null)
                .typeContrat(canSeePrivateData ? profile.getTypeContrat() : null)
                .dateDebutContrat(canSeePrivateData ? profile.getDateDebutContrat() : null)
                .dureeContrat(canSeePrivateData ? profile.getDureeContrat() : null)
                .payFrequency(canSeePrivateData ? profile.getPayFrequency() : null)
                .employmentFraction(canSeePrivateData ? profile.getEmploymentFraction() : null)
                .seniorityLevel(canSeePrivateData ? profile.getSeniorityLevel() : null)
                .signatureNumerique(canSeePrivateData ? profile.getSignatureNumerique() : null)
                // Collections
                .skills(skills)
                .documents(canSeePrivateData ? documents : null)
                .build();
    }

    /**
     * Détermine si le requérant peut accéder aux données privées [P] du profil.
     * Retourne true si :
     *  - L'utilisateur connecté est le propriétaire du profil (même email)
     *  - L'utilisateur connecté a le rôle HR_ADMIN ou DIRECTION
     */
    private boolean resolvePrivateDataAccess(EmployeeProfile profile) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }

        String currentUserEmail = auth.getName(); // Spring Security stores email as principal name

        // Check if the requester is the profile owner
        if (profile.getUser() != null && currentUserEmail.equalsIgnoreCase(profile.getUser().getEmail())) {
            return true;
        }

        // Check if the requester has an authorized role
        boolean isAuthorizedRole = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    return authority.equals("ROLE_HR_ADMIN")
                            || authority.equals("ROLE_DIRECTION")
                            || authority.equals("HR_ADMIN")
                            || authority.equals("DIRECTION");
                });

        return isAuthorizedRole;
    }

    /**
     * Calcule une chaîne formatée représentant l'ancienneté d'un employé.
     * Méthode utilitaire exposée pour les autres services si nécessaire.
     */
    public static String computeTimeInJob(LocalDate startDate) {
        if (startDate == null) return null;
        Period period = Period.between(startDate, LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();
        if (years > 0 && months > 0) {
            return years + " an" + (years > 1 ? "s" : "") + ", " + months + " mois";
        } else if (years > 0) {
            return years + " an" + (years > 1 ? "s" : "");
        } else if (months > 0) {
            return months + " mois";
        }
        return "Moins d'un mois";
    }
}
