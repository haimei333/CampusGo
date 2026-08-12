-- 知识库文档表
CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL DEFAULT 'GENERAL',
    content TEXT NOT NULL,
    tags VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 全文搜索索引
CREATE INDEX idx_knowledge_document_search
    ON knowledge_document
    USING GIN (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '') || ' ' || coalesce(tags, '')));

CREATE INDEX idx_knowledge_document_category ON knowledge_document(category);

COMMENT ON TABLE knowledge_document IS 'RAG 知识库文档表，用于 AI 助手检索增强';
COMMENT ON COLUMN knowledge_document.title IS '文档标题';
COMMENT ON COLUMN knowledge_document.category IS '分类：GENERAL-通用, FAQ-常见问题, PLATFORM-平台功能, TASK-任务指南, POINTS-积分系统, VOUCHER-优惠券, WALLET-钱包';
COMMENT ON COLUMN knowledge_document.content IS '文档内容';
COMMENT ON COLUMN knowledge_document.tags IS '标签，逗号分隔，用于辅助检索';