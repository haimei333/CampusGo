package com.campusgo.application.chat;

import com.campusgo.domain.model.ChatConversation;
import com.campusgo.domain.model.ChatMessage;

import java.util.List;

public interface ChatService {

    List<ConversationView> listConversations(long userId);

    ConversationView getConversation(long userId, long conversationId);

    ConversationView getOrCreateByTask(long userId, long taskId);

    List<ChatMessage> listMessages(long userId, long conversationId, Long beforeId, int limit);

    ChatMessage sendText(long userId, long conversationId, String content);

    void markRead(long userId, long conversationId);

    /** 抢单/确认预约成功后建会话（幂等） */
    ChatConversation ensureForAcceptedTask(long taskId, long publisherId, long runnerId);

    record ConversationView(
            ChatConversation conversation,
            String peerNickname,
            String peerRole,
            String taskTitle,
            int taskRewardCent,
            int unreadCount
    ) {
    }
}
