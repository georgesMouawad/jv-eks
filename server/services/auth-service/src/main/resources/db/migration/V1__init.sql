-- V1: initial schema for auth-service (Spring Security compatible)
CREATE TABLE IF NOT EXISTS users (
    id            UUID        PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    enabled       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP   NOT NULL
);

-- Spring Security authorities table
-- Many authorities can be assigned to one user (via user_id)
CREATE TABLE IF NOT EXISTS user_authorities (
    user_id       UUID        NOT NULL,
    authority     VARCHAR(50) NOT NULL,
    CONSTRAINT uk_user_authorities UNIQUE(user_id, authority),
    CONSTRAINT fk_user_authorities_users FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
