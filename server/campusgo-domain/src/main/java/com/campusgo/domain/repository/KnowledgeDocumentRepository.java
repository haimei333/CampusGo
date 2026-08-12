package com.campusgo.domain.repository;

import com.campusgo.domain.model.KnowledgeDocument;

import java.util.List;
import java.util.Optional;

/**
 * 知识库文档仓储接口
 */
public interface KnowledgeDocumentRepository {

    KnowledgeDocument save(KnowledgeDocument document);

    Optional<KnowledgeDocument> findById(Long id);

    List<KnowledgeDocument> findAll();

    List<KnowledgeDocument> findByCategory(String category);

    /**
     * 全文搜索知识库文档，按相关性排序
     */
    List<KnowledgeDocument> search(String query, int limit);

    void deleteById(Long id);
}