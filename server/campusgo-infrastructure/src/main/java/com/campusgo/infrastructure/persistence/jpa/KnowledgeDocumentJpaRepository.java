package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {

    List<KnowledgeDocumentEntity> findByCategory(String category);

    /**
     * 使用 PostgreSQL 全文搜索，按相关性排序
     */
    @Query(value = """
            SELECT * FROM knowledge_document
            WHERE to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '') || ' ' || coalesce(tags, ''))
                  @@ plainto_tsquery('simple', :query)
            ORDER BY ts_rank(
                to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '') || ' ' || coalesce(tags, '')),
                plainto_tsquery('simple', :query)
            ) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeDocumentEntity> searchByFullText(@Param("query") String query, @Param("limit") int limit);
}