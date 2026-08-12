package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("""
            SELECT m FROM ChatMessageEntity m
            WHERE m.conversationId = :conversationId
              AND (:beforeId IS NULL OR m.id < :beforeId)
            ORDER BY m.id DESC
            """)
    List<ChatMessageEntity> findPage(@Param("conversationId") Long conversationId,
                                     @Param("beforeId") Long beforeId,
                                     Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM ChatMessageEntity m
            WHERE m.conversationId = :conversationId
              AND m.readFlag = 0
              AND m.senderId IS NOT NULL
              AND m.senderId <> :excludeSenderId
            """)
    int countUnread(@Param("conversationId") Long conversationId,
                    @Param("excludeSenderId") Long excludeSenderId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ChatMessageEntity m
            SET m.readFlag = 1
            WHERE m.conversationId = :conversationId
              AND m.readFlag = 0
              AND m.senderId IS NOT NULL
              AND m.senderId <> :readerId
            """)
    int markReadByPeer(@Param("conversationId") Long conversationId,
                       @Param("readerId") Long readerId);
}
