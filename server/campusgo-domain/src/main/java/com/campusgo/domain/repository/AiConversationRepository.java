package com.campusgo.domain.repository;

import com.campusgo.domain.model.AiConversation;

import java.util.List;

/**
 * AI 对话仓储接口
 */
public interface AiConversationRepository {
    
    /**
     * 保存对话记录
     */
    AiConversation save(AiConversation conversation);
    
    /**
     * 根据用户ID和会话ID查询对话历史
     */
    List<AiConversation> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
    
    /**
     * 查询用户的所有会话ID
     */
    List<String> findDistinctSessionIdsByUserId(Long userId);
    
    /**
     * 删除指定会话的所有记录
     */
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
}
