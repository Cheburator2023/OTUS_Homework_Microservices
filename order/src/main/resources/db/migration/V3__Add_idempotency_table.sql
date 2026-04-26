CREATE TABLE IF NOT EXISTS idempotency_records (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_idempotency_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );

CREATE INDEX idx_idempotency_key ON idempotency_records(idempotency_key);