ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'defaultPassword123';
CREATE INDEX idx_users_email ON users(email);