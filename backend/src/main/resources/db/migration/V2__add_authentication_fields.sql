-- Add authentication fields to users table
ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255),
    ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';
