package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    int countByUserIdAndReadFlag(Long userId, short readFlag);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationEntity n SET n.readFlag = 1 WHERE n.userId = :userId AND n.id = :id")
    int markRead(@Param("userId") Long userId, @Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationEntity n SET n.readFlag = 1 WHERE n.userId = :userId AND n.readFlag = 0")
    int markAllRead(@Param("userId") Long userId);
}
