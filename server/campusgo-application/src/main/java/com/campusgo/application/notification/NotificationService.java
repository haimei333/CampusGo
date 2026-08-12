package com.campusgo.application.notification;

import com.campusgo.domain.enums.NotificationBizType;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.AppNotificationRecord;
import com.campusgo.domain.model.Task;

import java.util.List;

public interface NotificationService {

    List<AppNotificationRecord> list(long userId);

    int unreadCount(long userId);

    void markRead(long userId, long notificationId);

    void markAllRead(long userId);

    void notifyTaskAccepted(Task task, String runnerNickname);

    void notifyTaskCompleted(Task task);

    void notifyTaskCancelled(Task task, long notifyUserId, String reason);

    void notifyReviewPending(long userId, Task task);

    void notifyChatMessage(long userId, long conversationId, long taskId,
                           String peerName, String taskTitle, String preview);
}
