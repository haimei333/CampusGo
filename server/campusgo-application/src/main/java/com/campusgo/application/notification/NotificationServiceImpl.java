package com.campusgo.application.notification;

import com.campusgo.domain.enums.NotificationBizType;
import com.campusgo.domain.model.AppNotificationRecord;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int LIST_LIMIT = 50;

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AppNotificationRecord> list(long userId) {
        return notificationRepository.listByUser(userId, LIST_LIMIT);
    }

    @Override
    @Transactional(readOnly = true)
    public int unreadCount(long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Override
    @Transactional
    public void markRead(long userId, long notificationId) {
        notificationRepository.markRead(userId, notificationId);
    }

    @Override
    @Transactional
    public void markAllRead(long userId) {
        notificationRepository.markAllRead(userId);
    }

    @Override
    @Transactional
    public void notifyTaskAccepted(Task task, String runnerNickname) {
        save(task.getPublisherId(), "您的任务已被接单",
                runnerNickname + " 接取了「" + task.getTitle() + "」",
                NotificationBizType.TASK_ACCEPTED,
                String.valueOf(task.getId()), task);
    }

    @Override
    @Transactional
    public void notifyTaskCompleted(Task task) {
        if (task.getRunnerId() != null) {
            save(task.getRunnerId(), "订单已完成",
                    "「" + task.getTitle() + "」已确认完成，酬劳已到账",
                    NotificationBizType.TASK_COMPLETED,
                    String.valueOf(task.getId()), task);
        }
        notifyReviewPending(task.getPublisherId(), task);
        if (task.getRunnerId() != null) {
            notifyReviewPending(task.getRunnerId(), task);
        }
    }

    @Override
    @Transactional
    public void notifyTaskCancelled(Task task, long notifyUserId, String reason) {
        String body = reason != null && !reason.isBlank()
                ? "「" + task.getTitle() + "」已取消：" + reason
                : "「" + task.getTitle() + "」已取消";
        save(notifyUserId, "任务已取消", body,
                NotificationBizType.TASK_CANCELLED,
                String.valueOf(task.getId()), task);
    }

    @Override
    @Transactional
    public void notifyReviewPending(long userId, Task task) {
        save(userId, "请评价本次任务",
                "「" + task.getTitle() + "」已完成，快来评价吧",
                NotificationBizType.REVIEW_PENDING,
                String.valueOf(task.getId()), task);
    }

    @Override
    @Transactional
    public void notifyChatMessage(long userId, long conversationId, long taskId,
                                  String peerName, String taskTitle, String preview) {
        notificationRepository.save(AppNotificationRecord.builder()
                .userId(userId)
                .title("新消息")
                .body(peerName + "：" + preview)
                .bizType(NotificationBizType.CHAT_MESSAGE)
                .bizId(String.valueOf(conversationId))
                .taskId(taskId)
                .chatPeerName(peerName)
                .chatTaskTitle(taskTitle)
                .read(false)
                .createdAt(Instant.now())
                .build());
    }

    private void save(long userId, String title, String body,
                      NotificationBizType bizType, String bizId, Task task) {
        notificationRepository.save(AppNotificationRecord.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .bizType(bizType)
                .bizId(bizId)
                .taskId(task.getId())
                .taskStatus(mapTaskStatus(bizType, task))
                .taskMode(task.getMode())
                .chatTaskTitle(task.getTitle())
                .read(false)
                .createdAt(Instant.now())
                .build());
    }

    private static com.campusgo.domain.enums.TaskStatus mapTaskStatus(
            NotificationBizType bizType, Task task) {
        return switch (bizType) {
            case TASK_ACCEPTED -> com.campusgo.domain.enums.TaskStatus.ACCEPTED;
            case TASK_COMPLETED, REVIEW_PENDING -> com.campusgo.domain.enums.TaskStatus.COMPLETED;
            case TASK_CANCELLED -> com.campusgo.domain.enums.TaskStatus.CANCELLED;
            default -> task.getStatus();
        };
    }
}
