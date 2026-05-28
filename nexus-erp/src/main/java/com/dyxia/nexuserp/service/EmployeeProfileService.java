package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.dto.*;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.repository.EmployeeProfileRepository;
import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier des profils d'employés (CRUD et transformations DTO).
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

        EmployeeProfile profile = EmployeeProfile.builder()
                .user(user)
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .rib(request.getRib())
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

        if (request.getRib() != null) {
            profile.setRib(request.getRib());
        }

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

    /**
     * Convertit une entité {@link EmployeeProfile} en DTO {@link EmployeeProfileResponse}.
     * Omet de manière sécurisée la donnée brute sensible 'rib'.
     */
    private EmployeeProfileResponse mapToResponse(EmployeeProfile profile) {
        if (profile == null) return null;

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

        return EmployeeProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .firstName(profile.getUser() != null ? profile.getUser().getFirstName() : null)
                .lastName(profile.getUser() != null ? profile.getUser().getLastName() : null)
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .skills(skills)
                .documents(documents)
                .build();
    }
}
