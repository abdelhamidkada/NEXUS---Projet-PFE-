package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.TimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'accès aux données des pointages {@link TimeTracking}.
 */
@Repository
public interface TimeTrackingRepository extends JpaRepository<TimeTracking, UUID> {

    /**
     * Récupère tous les pointages d'un employé pour une période donnée, triés chronologiquement.
     *
     * @param employeeId  L'ID du profil employé.
     * @param startOfDay Le début de la journée.
     * @param endOfDay   La fin de la journée.
     * @return Liste ordonnée de pointages.
     */
    @Query("SELECT tt FROM TimeTracking tt " +
           "WHERE tt.employeeProfile.id = :employeeId " +
           "AND tt.timestamp >= :startOfDay " +
           "AND tt.timestamp < :endOfDay " +
           "ORDER BY tt.timestamp ASC")
    List<TimeTracking> findByEmployeeProfileIdAndTimestampBetween(
            @Param("employeeId") Long employeeId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
