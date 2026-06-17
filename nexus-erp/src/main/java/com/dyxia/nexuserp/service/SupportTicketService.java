package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.SupportTicket;
import com.dyxia.nexuserp.model.TicketPriority;
import com.dyxia.nexuserp.model.TicketStatus;
import com.dyxia.nexuserp.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service gérant la logique métier des tickets de support.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    /**
     * Crée un nouveau ticket de support et calcule sa date limite (SLA) selon sa priorité.
     */
    @Transactional
    public SupportTicket createTicket(SupportTicket ticket) {
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(LocalDateTime.now());
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        LocalDateTime createdAt = ticket.getCreatedAt();
        TicketPriority priority = ticket.getPriority();
        if (priority != null) {
            switch (priority) {
                case CRITICAL:
                    ticket.setDeadlineSla(createdAt.plusHours(4));
                    break;
                case HIGH:
                    ticket.setDeadlineSla(createdAt.plusHours(24));
                    break;
                case MEDIUM:
                    ticket.setDeadlineSla(createdAt.plusHours(48));
                    break;
                case LOW:
                    ticket.setDeadlineSla(createdAt.plusHours(72));
                    break;
            }
        }

        return supportTicketRepository.save(ticket);
    }

    /**
     * Récupère tous les tickets de support.
     */
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    /**
     * Récupère les tickets de support d'un employé.
     */
    public List<SupportTicket> getTicketsByEmployeeId(Long employeeId) {
        return supportTicketRepository.findByEmployeeId(employeeId);
    }

    /**
     * Met à jour le statut d'un ticket de support.
     */
    @Transactional
    public SupportTicket updateTicketStatus(Long id, TicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé avec l'ID : " + id));
        ticket.setStatus(status);
        return supportTicketRepository.save(ticket);
    }
}
