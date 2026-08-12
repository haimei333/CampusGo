-- AI 对话历史表
CREATE TABLE ai_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_conversation_user_id ON ai_conversation(user_id);
CREATE INDEX idx_ai_conversation_session_id ON ai_conversation(session_id);
CREATE INDEX idx_ai_conversation_created_at ON ai_conversation(created_at);

COMMENT ON TABLE ai_conversation IS 'AI 助手对话历史记录表';
COMMENT ON COLUMN ai_conversation.user_id IS '用户ID';
COMMENT ON COLUMN ai_conversation.session_id IS '会话ID，用于关联同一轮对话';
COMMENT ON COLUMN ai_conversation.role IS '角色：user-用户, assistant-AI助手, system-系统';
COMMENT ON COLUMN ai_conversation.content IS '对话内容';
COMMENT ON COLUMN ai_conversation.created_at IS '创建时间';
