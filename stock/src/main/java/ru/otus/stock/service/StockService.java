package ru.otus.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.stock.dto.ReleaseRequest;
import ru.otus.stock.dto.ReserveRequest;
import ru.otus.stock.dto.StockResponse;
import ru.otus.stock.exception.InsufficientStockException;
import ru.otus.stock.exception.ProductNotFoundException;
import ru.otus.stock.model.StockItem;
import ru.otus.stock.repository.StockItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockRepository;

    @Transactional
    public StockResponse reserve(ReserveRequest request) {
        StockItem item = stockRepository.findByProductId(request.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.productId()));

        int available = item.getQuantity() - item.getReservedQuantity();
        if (available < request.quantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.productId() +
                            ". Available: " + available + ", requested: " + request.quantity());
        }

        item.setReservedQuantity(item.getReservedQuantity() + request.quantity());
        stockRepository.save(item);
        log.info("Reserved {} units of product {} for order {}", request.quantity(), request.productId(), request.orderId());
        return new StockResponse(true, "Reserved successfully");
    }

    @Transactional
    public void release(ReleaseRequest request) {
        StockItem item = stockRepository.findByProductId(request.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.productId()));

        int newReserved = Math.max(0, item.getReservedQuantity() - request.quantity());
        item.setReservedQuantity(newReserved);
        stockRepository.save(item);
        log.info("Released {} units of product {} for order {}", request.quantity(), request.productId(), request.orderId());
    }
}