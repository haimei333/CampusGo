package com.campusgo.domain.repository;

import com.campusgo.domain.model.AppNotificationRecord;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    AppNotificationRecord save(AppNotificationRecord record);

    List<AppNotificationRecord> listByUser(long userId, int limit);

    int countUnread(long userId);

    Optional<AppNotificationRecord> findById(long id);

    void markRead(long userId, long id);

    void markAllRead(long userId);
}
