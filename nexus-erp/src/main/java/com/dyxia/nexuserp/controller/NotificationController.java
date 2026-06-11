package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.dto.NotificationResponse;
import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.model.Notification;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.repository.NotificationRepository;
import com.dyxia.nexuserp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        List<NotificationResponse> response = notifications.stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée ou non autorisée."));

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }
}
