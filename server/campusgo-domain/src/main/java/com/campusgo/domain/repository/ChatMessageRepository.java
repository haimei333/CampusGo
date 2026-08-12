package com.campusgo.domain.repository;

import com.campusgo.domain.model.ChatMessage;

import java.util.List;

public interface ChatMessageRepository {

    List<ChatMessage> listByConversation(long conversationId, Long beforeId, int limit);

    ChatMessage save(ChatMessage message);

    int countUnread(long conversationId, long excludeSenderId);

    void markReadByPeer(long conversationId, long readerId);
}
