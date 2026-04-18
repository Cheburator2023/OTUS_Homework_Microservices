package ru.otus.delivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_slots")
@Getter
@NoArgsConstructor
public class DeliverySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_slot", nullable = false, unique = true)
    private LocalDateTime timeSlot;

    @Setter
    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Setter
    @Column(name = "order_id")
    private Long orderId; // null, если слот свободен

    @Setter
    @Column(nullable = false)
    private boolean reserved = false;

    public DeliverySlot(LocalDateTime timeSlot, Long courierId) {
        this.timeSlot = timeSlot;
        this.courierId = courierId;
    }
}