-- Phase 3: user address book

CREATE TABLE IF NOT EXISTS user_address (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(40)  NOT NULL,
    detail          VARCHAR(256) NOT NULL DEFAULT '',
    lng             NUMERIC(10, 7) NOT NULL DEFAULT 116.310003,
    lat             NUMERIC(10, 7) NOT NULL DEFAULT 39.992801,
    tag             VARCHAR(16)  NOT NULL DEFAULT 'OTHER',
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    use_count       INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_addr_user ON user_address (user_id);
