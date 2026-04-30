-- V1: initial schema for auth-service
CREATE TABLE IF NOT EXISTS users (
    id            UUID        PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    role          VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP   NOT NULL
);
