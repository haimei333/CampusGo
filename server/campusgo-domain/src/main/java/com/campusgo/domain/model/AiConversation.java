package com.campusgo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * AI 对话记录领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversation {
    private Long id;
    private Long userId;
    private String sessionId;
    private String role;
    private String content;
    private Instant createdAt;
}
