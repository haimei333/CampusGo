-- User Voucher: 用户兑换后持有的券

CREATE TABLE IF NOT EXISTS user_voucher (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(100) NOT NULL,
    voucher_code  VARCHAR(50)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'UNUSED',
    expire_at     TIMESTAMPTZ  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at       TIMESTAMPTZ,
    CONSTRAINT uk_voucher_code UNIQUE (voucher_code),
    CONSTRAINT fk_voucher_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_voucher_product FOREIGN KEY (product_id) REFERENCES mall_product (id)
);

CREATE INDEX IF NOT EXISTS idx_voucher_user ON user_voucher (user_id, status);
CREATE INDEX IF NOT EXISTS idx_voucher_code ON user_voucher (voucher_code);

COMMENT ON TABLE user_voucher IS '用户券包表：用户兑换商品后持有的券';
