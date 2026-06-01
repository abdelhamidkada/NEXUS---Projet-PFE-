package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository pour l'accès aux données de l'entité {@link LeaveRequest}.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
}
