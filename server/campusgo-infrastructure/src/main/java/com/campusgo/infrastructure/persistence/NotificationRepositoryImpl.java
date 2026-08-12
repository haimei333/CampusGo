package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.enums.NotificationBizType;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.AppNotificationRecord;
import com.campusgo.domain.repository.NotificationRepository;
import com.campusgo.infrastructure.persistence.entity.NotificationEntity;
import com.campusgo.infrastructure.persistence.jpa.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public AppNotificationRecord save(AppNotificationRecord record) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(record.getUserId());
        entity.setTitle(record.getTitle());
        entity.setBody(record.getBody());
        entity.setBizType(record.getBizType());
        entity.setBizId(record.getBizId());
        entity.setTaskId(record.getTaskId());
        entity.setTaskStatus(record.getTaskStatus());
        entity.setTaskMode(record.getTaskMode());
        entity.setChatPeerName(record.getChatPeerName());
        entity.setChatTaskTitle(record.getChatTaskTitle());
        entity.setReadFlag((short) (record.isRead() ? 1 : 0));
        if (record.getCreatedAt() != null) {
            entity.setCreatedAt(record.getCreatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<AppNotificationRecord> listByUser(long userId, int limit) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(Math.max(limit, 1))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countUnread(long userId) {
        return jpaRepository.countByUserIdAndReadFlag(userId, (short) 0);
    }

    @Override
    public Optional<AppNotificationRecord> findById(long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public void markRead(long userId, long id) {
        jpaRepository.markRead(userId, id);
    }

    @Override
    @Transactional
    public void markAllRead(long userId) {
        jpaRepository.markAllRead(userId);
    }

    private AppNotificationRecord toDomain(NotificationEntity entity) {
        return AppNotificationRecord.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .bizType(entity.getBizType())
                .bizId(entity.getBizId())
                .taskId(entity.getTaskId())
                .taskStatus(entity.getTaskStatus())
                .taskMode(entity.getTaskMode())
                .chatPeerName(entity.getChatPeerName())
                .chatTaskTitle(entity.getChatTaskTitle())
                .read(entity.getReadFlag() != 0)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
