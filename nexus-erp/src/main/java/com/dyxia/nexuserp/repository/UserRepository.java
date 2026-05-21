package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface Repository pour l'accès aux données de l'entité {@link User}.
 * Fournit des méthodes CRUD et des requêtes personnalisées.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse e-mail.
     * Indispensable pour le processus d'authentification et de chargement des utilisateurs.
     *
     * @param email L'adresse e-mail de l'utilisateur recherché.
     * @return Un {@link Optional} contenant l'utilisateur s'il existe, sinon vide.
     */
    Optional<User> findByEmail(String email);
}
