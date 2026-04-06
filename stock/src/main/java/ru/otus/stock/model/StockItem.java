package ru.otus.stock.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_items")
@Getter
@NoArgsConstructor
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Setter
    @Column(nullable = false)
    private Integer quantity;

    @Setter
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    public StockItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}