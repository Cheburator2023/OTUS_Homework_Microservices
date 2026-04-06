package ru.otus.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.delivery.model.DeliverySlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliverySlotRepository extends JpaRepository<DeliverySlot, Long> {
    List<DeliverySlot> findByTimeSlot(LocalDateTime timeSlot);

    Optional<DeliverySlot> findByOrderId(Long orderId);
}