package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.ChatConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatConversationJpaRepository extends JpaRepository<ChatConversationEntity, Long> {

    Optional<ChatConversationEntity> findByTaskId(Long taskId);

    @Query("""
            SELECT c FROM ChatConversationEntity c
            WHERE c.publisherId = :userId OR c.runnerId = :userId
            ORDER BY c.lastMsgAt DESC NULLS LAST, c.id DESC
            """)
    List<ChatConversationEntity> findByParticipant(@Param("userId") Long userId);
}
