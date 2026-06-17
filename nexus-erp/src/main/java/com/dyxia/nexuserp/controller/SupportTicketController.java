package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.model.SupportTicket;
import com.dyxia.nexuserp.model.TicketStatus;
import com.dyxia.nexuserp.service.SupportTicketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des tickets de support (Helpdesk).
 */
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    /**
     * Endpoint POST pour créer un ticket.
     */
    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        SupportTicket created = supportTicketService.createTicket(ticket);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Endpoint GET pour récupérer tous les tickets.
     */
    @GetMapping
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        return ResponseEntity.ok(supportTicketService.getAllTickets());
    }

    /**
     * Endpoint GET pour récupérer les tickets d'un employé.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SupportTicket>> getTicketsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(supportTicketService.getTicketsByEmployeeId(employeeId));
    }

    /**
     * Endpoint PUT pour mettre à jour le statut d'un ticket.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<SupportTicket> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody TicketStatusUpdateRequest updateRequest) {
        SupportTicket updated = supportTicketService.updateTicketStatus(id, updateRequest.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * DTO pour la mise à jour du statut.
     */
    @Data
    public static class TicketStatusUpdateRequest {
        private TicketStatus status;
    }
}
