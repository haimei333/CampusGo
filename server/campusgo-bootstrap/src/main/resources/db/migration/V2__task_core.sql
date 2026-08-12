-- Phase 2: task persistence + group members + wallet ledger

CREATE TABLE IF NOT EXISTS task (
    id                   BIGSERIAL PRIMARY KEY,
    task_no              VARCHAR(32)  NOT NULL,
    publisher_id         BIGINT       NOT NULL,
    runner_id            BIGINT,
    mode                 VARCHAR(16)  NOT NULL,
    category             VARCHAR(16)  NOT NULL,
    title                VARCHAR(60)  NOT NULL,
    description          VARCHAR(400),
    status               VARCHAR(16)  NOT NULL,
    pickup_name          VARCHAR(128) NOT NULL,
    pickup_detail        VARCHAR(256),
    pickup_lng           NUMERIC(10, 7) NOT NULL DEFAULT 116.397428,
    pickup_lat           NUMERIC(10, 7) NOT NULL DEFAULT 39.90923,
    dropoff_name         VARCHAR(128) NOT NULL,
    dropoff_detail       VARCHAR(256),
    dropoff_lng          NUMERIC(10, 7) NOT NULL DEFAULT 116.397428,
    dropoff_lat          NUMERIC(10, 7) NOT NULL DEFAULT 39.90923,
    expect_finish_at     TIMESTAMPTZ,
    reserve_at           TIMESTAMPTZ,
    time_label           VARCHAR(64),
    reward_cent          INTEGER      NOT NULL,
    base_reward_cent     INTEGER      NOT NULL,
    emergency_rate       INTEGER,
    escrow_cent          INTEGER      NOT NULL DEFAULT 0,
    group_target_count   INTEGER,
    group_joined_count   INTEGER,
    group_split_type     VARCHAR(16),
    delivery_photo_url   VARCHAR(512),
    cancel_reason        VARCHAR(256),
    accepted_at          TIMESTAMPTZ,
    delivering_at        TIMESTAMPTZ,
    confirming_at        TIMESTAMPTZ,
    completed_at         TIMESTAMPTZ,
    cancelled_at         TIMESTAMPTZ,
    version              INTEGER      NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_task_no UNIQUE (task_no),
    CONSTRAINT fk_task_publisher FOREIGN KEY (publisher_id) REFERENCES app_user (id),
    CONSTRAINT fk_task_runner FOREIGN KEY (runner_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_task_publisher_status ON task (publisher_id, status);
CREATE INDEX IF NOT EXISTS idx_task_runner_status ON task (runner_id, status);
CREATE INDEX IF NOT EXISTS idx_task_hall ON task (status, mode, created_at);

CREATE TABLE IF NOT EXISTS task_status_log (
    id           BIGSERIAL PRIMARY KEY,
    task_id      BIGINT       NOT NULL,
    from_status  VARCHAR(16),
    to_status    VARCHAR(16)  NOT NULL,
    operator_id  BIGINT,
    remark       VARCHAR(256),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tsl_task FOREIGN KEY (task_id) REFERENCES task (id)
);

CREATE INDEX IF NOT EXISTS idx_tsl_task ON task_status_log (task_id, created_at);

CREATE TABLE IF NOT EXISTS task_group_member (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT       NOT NULL,
    user_id         BIGINT,
    role            VARCHAR(16)  NOT NULL,
    name            VARCHAR(32)  NOT NULL DEFAULT '',
    address_summary VARCHAR(256) NOT NULL DEFAULT '',
    share_cent      INTEGER      NOT NULL DEFAULT 0,
    pay_status      VARCHAR(16)  NOT NULL DEFAULT 'UNPAID',
    joined_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tgm_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_tgm_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_tgm_task ON task_group_member (task_id);

CREATE TABLE IF NOT EXISTS wallet_ledger (
    id                  BIGSERIAL PRIMARY KEY,
    ledger_no           VARCHAR(32)  NOT NULL,
    user_id             BIGINT       NOT NULL,
    type                VARCHAR(32)  NOT NULL,
    amount_cent         INTEGER      NOT NULL,
    direction           VARCHAR(8)   NOT NULL,
    balance_after_cent  INTEGER      NOT NULL,
    task_id             BIGINT,
    remark              VARCHAR(256),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ledger_no UNIQUE (ledger_no),
    CONSTRAINT fk_ledger_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_ledger_task FOREIGN KEY (task_id) REFERENCES task (id)
);

CREATE INDEX IF NOT EXISTS idx_ledger_user_time ON wallet_ledger (user_id, created_at DESC);
