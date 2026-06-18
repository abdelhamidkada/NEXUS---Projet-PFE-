package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.EmployeeProfile;
import com.dyxia.nexuserp.model.HrDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface Repository pour l'accès aux données des documents RH (HrDocument).
 */
@Repository
public interface HrDocumentRepository extends JpaRepository<HrDocument, Long> {

    /**
     * Récupère tous les documents appartenant au profil d'un employé donné.
     *
     * @param employeeProfile Le profil de l'employé.
     * @return Liste des documents associés.
     */
    List<HrDocument> findByEmployeeProfile(EmployeeProfile employeeProfile);
}
