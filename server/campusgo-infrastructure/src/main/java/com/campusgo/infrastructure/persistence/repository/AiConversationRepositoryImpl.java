package com.campusgo.infrastructure.persistence.repository;

import com.campusgo.domain.model.AiConversation;
import com.campusgo.domain.repository.AiConversationRepository;
import com.campusgo.infrastructure.persistence.entity.AiConversationEntity;
import com.campusgo.infrastructure.persistence.jpa.AiConversationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AiConversationRepositoryImpl implements AiConversationRepository {

    private final AiConversationJpaRepository jpaRepository;

    @Override
    public AiConversation save(AiConversation conversation) {
        AiConversationEntity entity = toEntity(conversation);
        AiConversationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AiConversation> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId) {
        return jpaRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctSessionIdsByUserId(Long userId) {
        return jpaRepository.findDistinctSessionIdsByUserId(userId);
    }

    @Override
    public void deleteByUserIdAndSessionId(Long userId, String sessionId) {
        jpaRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    private AiConversationEntity toEntity(AiConversation domain) {
        AiConversationEntity entity = new AiConversationEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setSessionId(domain.getSessionId());
        entity.setRole(domain.getRole());
        entity.setContent(domain.getContent());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private AiConversation toDomain(AiConversationEntity entity) {
        return AiConversation.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .sessionId(entity.getSessionId())
                .role(entity.getRole())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
