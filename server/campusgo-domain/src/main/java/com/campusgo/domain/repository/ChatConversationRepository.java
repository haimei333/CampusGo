package com.campusgo.domain.repository;

import com.campusgo.domain.model.ChatConversation;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository {

    Optional<ChatConversation> findById(long id);

    Optional<ChatConversation> findByTaskId(long taskId);

    List<ChatConversation> listByUserId(long userId);

    ChatConversation save(ChatConversation conversation);
}
