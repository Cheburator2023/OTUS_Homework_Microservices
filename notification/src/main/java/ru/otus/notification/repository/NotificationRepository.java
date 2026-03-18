package ru.otus.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.notification.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}