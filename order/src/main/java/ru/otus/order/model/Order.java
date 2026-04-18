package ru.otus.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Setter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Новые поля
    @Setter
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Setter
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Setter
    @Column(name = "delivery_time", nullable = false)
    private LocalDateTime deliveryTime;

    public Order(Long userId, BigDecimal amount, OrderStatus status,
                 Long productId, Long quantity, LocalDateTime deliveryTime) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.productId = productId;
        this.quantity = quantity;
        this.deliveryTime = deliveryTime;
    }

    public enum OrderStatus {
        SUCCESS, FAILED
    }
}