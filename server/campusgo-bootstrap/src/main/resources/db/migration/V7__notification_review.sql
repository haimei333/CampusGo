-- Notification + Review (CampusGo-数据表结构设计.md §4.14 / §5.4)

CREATE TABLE IF NOT EXISTS notification (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    title           VARCHAR(128) NOT NULL,
    body            VARCHAR(512) NOT NULL,
    biz_type        VARCHAR(32)  NOT NULL,
    biz_id          VARCHAR(64),
    task_id         BIGINT,
    task_status     VARCHAR(16),
    task_mode       VARCHAR(16),
    chat_peer_name  VARCHAR(64),
    chat_task_title VARCHAR(128),
    read_flag       SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_notif_user_time ON notification (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_user_unread ON notification (user_id, read_flag);

CREATE TABLE IF NOT EXISTS review (
    id           BIGSERIAL PRIMARY KEY,
    task_id      BIGINT       NOT NULL,
    from_user_id BIGINT       NOT NULL,
    to_user_id   BIGINT       NOT NULL,
    score        SMALLINT     NOT NULL,
    tags_json    TEXT,
    content      VARCHAR(400),
    is_default   SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_review_task_from UNIQUE (task_id, from_user_id),
    CONSTRAINT fk_review_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_review_from FOREIGN KEY (from_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_review_to FOREIGN KEY (to_user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_review_task ON review (task_id);
