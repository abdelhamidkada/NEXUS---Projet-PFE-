package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface Repository pour l'accès aux données de l'entité {@link EmployeeProfile}.
 * Fournit des méthodes CRUD et des requêtes optimisées avec jointures FETCH.
 */
@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    /**
     * Récupère tous les profils d'employés en chargeant de manière optimisée l'utilisateur associé.
     * Évite le problème classique des requêtes N+1.
     *
     * @return Liste de tous les profils d'employés avec leurs données utilisateur.
     */
    @Query("SELECT ep FROM EmployeeProfile ep LEFT JOIN FETCH ep.user")
    List<EmployeeProfile> findAllWithUser();

    /**
     * Vérifie si un profil d'employé existe déjà pour un utilisateur donné.
     *
     * @param userId Identifiant de l'utilisateur.
     * @return true si le profil existe, sinon false.
     */
    boolean existsByUserId(Long userId);
}
