-- Points Wallet, Check-in, Mall & Redeem (CampusGo-数据表结构设计.md §4.15 / §4.16 / §4.17)

CREATE TABLE IF NOT EXISTS points_wallet (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    balance       INTEGER      NOT NULL DEFAULT 0,
    total_earned  INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_points_wallet_user UNIQUE (user_id),
    CONSTRAINT fk_points_wallet_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE IF NOT EXISTS points_transaction (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    amount        INTEGER      NOT NULL,
    balance_after INTEGER      NOT NULL,
    biz_type      VARCHAR(30)  NOT NULL,
    biz_id        VARCHAR(50),
    remark        VARCHAR(200),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_points_txn_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_points_txn_user_time ON points_transaction (user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS check_in_record (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    check_in_date DATE         NOT NULL,
    streak        INTEGER      NOT NULL DEFAULT 1,
    reward_points INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_checkin_user_date UNIQUE (user_id, check_in_date),
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_checkin_user_date ON check_in_record (user_id, check_in_date DESC);

CREATE TABLE IF NOT EXISTS mall_product (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    subtitle      VARCHAR(200),
    category      VARCHAR(20)  NOT NULL,
    points_cost   INTEGER      NOT NULL,
    stock         INTEGER      NOT NULL DEFAULT 0,
    emoji         VARCHAR(10),
    flash_sale    BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS redeem_record (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(100) NOT NULL,
    points_cost   INTEGER      NOT NULL,
    address       VARCHAR(300),
    status        VARCHAR(20)  NOT NULL DEFAULT 'DELIVERED',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_redeem_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_redeem_product FOREIGN KEY (product_id) REFERENCES mall_product (id)
);

CREATE INDEX IF NOT EXISTS idx_redeem_user_time ON redeem_record (user_id, created_at DESC);