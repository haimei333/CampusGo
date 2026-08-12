-- Chat: conversation + message (CampusGo-数据表结构设计.md §4.12–4.13)

CREATE TABLE IF NOT EXISTS chat_conversation (
    id               BIGSERIAL PRIMARY KEY,
    task_id          BIGINT       NOT NULL,
    publisher_id     BIGINT       NOT NULL,
    runner_id        BIGINT       NOT NULL,
    last_msg_preview VARCHAR(200),
    last_msg_at      TIMESTAMPTZ,
    archived         SMALLINT     NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_conv_task UNIQUE (task_id),
    CONSTRAINT fk_conv_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_conv_publisher FOREIGN KEY (publisher_id) REFERENCES app_user (id),
    CONSTRAINT fk_conv_runner FOREIGN KEY (runner_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_conv_publisher_time ON chat_conversation (publisher_id, last_msg_at DESC NULLS LAST);
CREATE INDEX IF NOT EXISTS idx_conv_runner_time ON chat_conversation (runner_id, last_msg_at DESC NULLS LAST);

CREATE TABLE IF NOT EXISTS chat_message (
    id               BIGSERIAL PRIMARY KEY,
    conversation_id  BIGINT        NOT NULL,
    task_id          BIGINT        NOT NULL,
    sender_id        BIGINT,
    msg_type         VARCHAR(16)   NOT NULL,
    content          VARCHAR(2000) NOT NULL,
    read_flag        SMALLINT      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES chat_conversation (id),
    CONSTRAINT fk_msg_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_msg_conv_time ON chat_message (conversation_id, id);
CREATE INDEX IF NOT EXISTS idx_msg_unread ON chat_message (conversation_id, read_flag, sender_id);
