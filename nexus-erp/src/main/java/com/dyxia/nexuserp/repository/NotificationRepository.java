package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.Notification;
import com.dyxia.nexuserp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
}
