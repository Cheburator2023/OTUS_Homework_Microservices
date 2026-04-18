package ru.otus.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "idempotency_records")
@Getter
@NoArgsConstructor
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Setter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public IdempotencyRecord(String idempotencyKey, Order order) {
        this.idempotencyKey = idempotencyKey;
        this.order = order;
    }
}