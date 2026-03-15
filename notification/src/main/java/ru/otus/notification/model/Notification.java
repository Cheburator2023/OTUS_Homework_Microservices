package ru.otus.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String message;

    @Setter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Notification(Long userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }
}