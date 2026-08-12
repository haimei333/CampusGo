package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.AiConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiConversationJpaRepository extends JpaRepository<AiConversationEntity, Long> {

    List<AiConversationEntity> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    List<AiConversationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT DISTINCT a.sessionId FROM AiConversationEntity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<String> findDistinctSessionIdsByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndSessionId(Long userId, String sessionId);
}
