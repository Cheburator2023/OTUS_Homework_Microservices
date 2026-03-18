CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          balance DECIMAL(19,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);