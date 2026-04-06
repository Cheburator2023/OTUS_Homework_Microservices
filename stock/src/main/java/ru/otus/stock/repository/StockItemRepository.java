package ru.otus.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.stock.model.StockItem;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductId(Long productId);
}