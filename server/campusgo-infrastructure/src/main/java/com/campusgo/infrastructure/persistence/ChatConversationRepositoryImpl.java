package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.ChatConversation;
import com.campusgo.domain.repository.ChatConversationRepository;
import com.campusgo.infrastructure.persistence.entity.ChatConversationEntity;
import com.campusgo.infrastructure.persistence.jpa.ChatConversationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatConversationRepositoryImpl implements ChatConversationRepository {

    private final ChatConversationJpaRepository jpaRepository;

    @Override
    public Optional<ChatConversation> findById(long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ChatConversation> findByTaskId(long taskId) {
        return jpaRepository.findByTaskId(taskId).map(this::toDomain);
    }

    @Override
    public List<ChatConversation> listByUserId(long userId) {
        return jpaRepository.findByParticipant(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ChatConversation save(ChatConversation conversation) {
        ChatConversationEntity entity;
        if (conversation.getId() > 0) {
            entity = jpaRepository.findById(conversation.getId()).orElseGet(ChatConversationEntity::new);
        } else {
            entity = new ChatConversationEntity();
        }
        entity.setTaskId(conversation.getTaskId());
        entity.setPublisherId(conversation.getPublisherId());
        entity.setRunnerId(conversation.getRunnerId());
        entity.setLastMsgPreview(conversation.getLastMsgPreview());
        entity.setLastMsgAt(conversation.getLastMsgAt());
        entity.setArchived((short) (conversation.isArchived() ? 1 : 0));
        if (conversation.getCreatedAt() != null) {
            entity.setCreatedAt(conversation.getCreatedAt());
        }
        if (conversation.getUpdatedAt() != null) {
            entity.setUpdatedAt(conversation.getUpdatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    private ChatConversation toDomain(ChatConversationEntity entity) {
        return ChatConversation.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .publisherId(entity.getPublisherId())
                .runnerId(entity.getRunnerId())
                .lastMsgPreview(entity.getLastMsgPreview())
                .lastMsgAt(entity.getLastMsgAt())
                .archived(entity.getArchived() != 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
