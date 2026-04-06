CREATE TABLE stock_items (
                             id BIGSERIAL PRIMARY KEY,
                             product_id BIGINT NOT NULL UNIQUE,
                             quantity INTEGER NOT NULL,
                             reserved_quantity INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_stock_items_product_id ON stock_items(product_id);

-- Начальные данные для тестирования
INSERT INTO stock_items (product_id, quantity) VALUES (1, 100);
INSERT INTO stock_items (product_id, quantity) VALUES (2, 50);