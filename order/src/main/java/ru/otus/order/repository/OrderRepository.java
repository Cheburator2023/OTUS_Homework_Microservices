package ru.otus.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}