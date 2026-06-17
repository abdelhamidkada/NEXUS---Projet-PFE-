package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface Repository pour l'accès aux données des Refresh Tokens.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Recherche un Refresh Token par sa valeur de chaîne.
     *
     * @param token Le jeton de rafraîchissement sous forme de chaîne de caractères.
     * @return Un {@link Optional} contenant le RefreshToken s'il est trouvé.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Supprime tous les Refresh Tokens associés à un utilisateur spécifique.
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
