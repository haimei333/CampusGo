-- Phase 3: reservation hold slots

CREATE TABLE IF NOT EXISTS task_reserve_slot (
    id                BIGSERIAL PRIMARY KEY,
    task_id           BIGINT       NOT NULL,
    runner_id         BIGINT       NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'HOLDING',
    hold_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    confirm_deadline  TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_reserve_task_runner UNIQUE (task_id, runner_id),
    CONSTRAINT fk_reserve_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_reserve_runner FOREIGN KEY (runner_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_reserve_task ON task_reserve_slot (task_id);
CREATE INDEX IF NOT EXISTS idx_reserve_runner ON task_reserve_slot (runner_id);
