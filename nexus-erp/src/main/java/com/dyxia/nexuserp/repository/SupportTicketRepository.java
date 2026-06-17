package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'accès aux données de l'entité {@link SupportTicket}.
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByEmployeeId(Long employeeId);
}
