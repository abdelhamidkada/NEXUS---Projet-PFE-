package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface Repository pour l'accès aux données de l'entité {@link Role}.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Recherche un rôle par son nom (ex: "EMPLOYEE", "MANAGER").
     *
     * @param name Le nom du rôle.
     * @return Un {@link Optional} contenant le rôle s'il existe, sinon vide.
     */
    Optional<Role> findByName(String name);
}
