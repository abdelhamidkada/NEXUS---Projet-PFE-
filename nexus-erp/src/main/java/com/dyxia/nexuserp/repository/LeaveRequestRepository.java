package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'accès aux données de l'entité {@link LeaveRequest}.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    @Query("SELECT lr FROM LeaveRequest lr " +
           "WHERE lr.employeeProfile.id = :employeeId " +
           "AND (lr.status = com.dyxia.nexuserp.model.LeaveStatus.VALIDATED_N1 OR lr.status = com.dyxia.nexuserp.model.LeaveStatus.PROCESSED_HR) " +
           "AND :date BETWEEN lr.startDate AND lr.endDate")
    List<LeaveRequest> findValidatedLeavesForToday(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
           "WHERE lr.employeeProfile.id = :employeeId " +
           "AND (lr.status = com.dyxia.nexuserp.model.LeaveStatus.VALIDATED_N1 OR lr.status = com.dyxia.nexuserp.model.LeaveStatus.PROCESSED_HR) " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findValidatedLeavesForRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
