package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.ChatMessage;
import com.campusgo.domain.repository.ChatMessageRepository;
import com.campusgo.infrastructure.persistence.entity.ChatMessageEntity;
import com.campusgo.infrastructure.persistence.jpa.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageJpaRepository jpaRepository;

    @Override
    public List<ChatMessage> listByConversation(long conversationId, Long beforeId, int limit) {
        List<ChatMessageEntity> desc = jpaRepository.findPage(
                conversationId, beforeId, PageRequest.of(0, limit));
        List<ChatMessage> result = new ArrayList<>(desc.size());
        for (int i = desc.size() - 1; i >= 0; i--) {
            result.add(toDomain(desc.get(i)));
        }
        return result;
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setConversationId(message.getConversationId());
        entity.setTaskId(message.getTaskId());
        entity.setSenderId(message.getSenderId());
        entity.setMsgType(message.getMsgType());
        entity.setContent(message.getContent());
        entity.setReadFlag((short) (message.isRead() ? 1 : 0));
        if (message.getCreatedAt() != null) {
            entity.setCreatedAt(message.getCreatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public int countUnread(long conversationId, long excludeSenderId) {
        return jpaRepository.countUnread(conversationId, excludeSenderId);
    }

    @Override
    @Transactional
    public void markReadByPeer(long conversationId, long readerId) {
        jpaRepository.markReadByPeer(conversationId, readerId);
    }

    private ChatMessage toDomain(ChatMessageEntity entity) {
        return ChatMessage.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .taskId(entity.getTaskId())
                .senderId(entity.getSenderId())
                .msgType(entity.getMsgType())
                .content(entity.getContent())
                .read(entity.getReadFlag() != 0)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
