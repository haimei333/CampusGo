package com.campusgo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 知识库文档领域模型，用于 RAG 检索增强
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {
    private Long id;
    private String title;
    private String category;
    private String content;
    private String tags;
    private Instant createdAt;
    private Instant updatedAt;
}