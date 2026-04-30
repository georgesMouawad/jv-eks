-- V1: initial schema for crate-service
CREATE TABLE IF NOT EXISTS crates (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    owner_id   UUID         NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS crate_items (
    id          UUID         PRIMARY KEY,
    crate_id    UUID         NOT NULL REFERENCES crates(id),
    track_name  VARCHAR(200) NOT NULL,
    s3_key      TEXT         NOT NULL,
    added_by    UUID         NOT NULL,
    added_at    TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_crate_items_crate_id ON crate_items(crate_id);
