-- CampusGo P0 core tables (subset of CampusGo-数据表结构设计.md)

CREATE TABLE IF NOT EXISTS app_user (
    id              BIGSERIAL PRIMARY KEY,
    phone           VARCHAR(20)  NOT NULL,
    nickname        VARCHAR(32)  NOT NULL,
    avatar_url      VARCHAR(512),
    credit_score    INTEGER      NOT NULL DEFAULT 500,
    active_role     VARCHAR(16)  NOT NULL DEFAULT 'PUBLISHER',
    campus_status   VARCHAR(16)  NOT NULL DEFAULT 'NONE',
    status          SMALLINT     NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_app_user_phone UNIQUE (phone)
);

CREATE INDEX IF NOT EXISTS idx_app_user_credit ON app_user (credit_score);

CREATE TABLE IF NOT EXISTS wallet (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    balance_cent        INTEGER      NOT NULL DEFAULT 0,
    frozen_cent         INTEGER      NOT NULL DEFAULT 0,
    total_income_cent   INTEGER      NOT NULL DEFAULT 0,
    total_withdraw_cent INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_wallet_user UNIQUE (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
