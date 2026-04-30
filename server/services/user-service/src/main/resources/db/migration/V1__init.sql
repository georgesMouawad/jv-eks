-- V1: initial schema for user-service
CREATE TABLE IF NOT EXISTS user_profiles (
    id         UUID         PRIMARY KEY,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    bio        VARCHAR(500),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
