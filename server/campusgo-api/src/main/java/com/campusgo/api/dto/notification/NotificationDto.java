package com.campusgo.api.dto.notification;

import com.campusgo.domain.enums.NotificationBizType;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.AppNotificationRecord;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationDto {

    private String id;
    private String title;
    private String body;
    private String timeLabel;
    private boolean unread;
    private String linkType;
    private String bizType;
    private String linkTargetId;
    private TaskStatus taskStatus;
    private TaskMode taskMode;
    private String chatPeerName;
    private String chatTaskTitle;
    private String chatTaskId;
    private Instant createdAt;

    public static NotificationDto from(AppNotificationRecord record) {
        LinkMapping link = mapLink(record.getBizType(), record);
        return NotificationDto.builder()
                .id(String.valueOf(record.getId()))
                .title(record.getTitle())
                .body(record.getBody())
                .timeLabel("")
                .unread(!record.isRead())
                .linkType(link.linkType())
                .bizType(record.getBizType() != null ? record.getBizType().name() : null)
                .linkTargetId(link.linkTargetId())
                .taskStatus(record.getTaskStatus())
                .taskMode(record.getTaskMode())
                .chatPeerName(record.getChatPeerName())
                .chatTaskTitle(record.getChatTaskTitle())
                .chatTaskId(record.getTaskId() == null ? null : String.valueOf(record.getTaskId()))
                .createdAt(record.getCreatedAt())
                .build();
    }

    private static LinkMapping mapLink(NotificationBizType bizType, AppNotificationRecord record) {
        return switch (bizType) {
            case TASK_ACCEPTED, TASK_COMPLETED, TASK_CANCELLED -> new LinkMapping(
                    "TASK",
                    record.getBizId() != null ? record.getBizId()
                            : (record.getTaskId() == null ? null : String.valueOf(record.getTaskId())));
            case REVIEW_PENDING -> new LinkMapping(
                    "REVIEW",
                    record.getBizId() != null ? record.getBizId()
                            : (record.getTaskId() == null ? null : String.valueOf(record.getTaskId())));
            case CHAT_MESSAGE -> new LinkMapping("CHAT", record.getBizId());
        };
    }

    private record LinkMapping(String linkType, String linkTargetId) {
    }
}
