package com.campusgo.domain.model;

import com.campusgo.domain.enums.NotificationBizType;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AppNotificationRecord {
    long id;
    long userId;
    String title;
    String body;
    NotificationBizType bizType;
    String bizId;
    Long taskId;
    TaskStatus taskStatus;
    TaskMode taskMode;
    String chatPeerName;
    String chatTaskTitle;
    boolean read;
    Instant createdAt;
}
