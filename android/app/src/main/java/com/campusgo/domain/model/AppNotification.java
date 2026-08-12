package com.campusgo.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * M03 通知列表项
 */
public class AppNotification {

    public enum LinkType {
        NONE,
        TASK,
        REVIEW,
        GROUP,
        CHAT,
        HELP
    }

    public final String id;
    public final String title;
    public final String body;
    public final String timeLabel;
    public boolean unread;
    @NonNull
    public final LinkType linkType;
    @Nullable
    public final String linkTargetId;
    @Nullable
    public final TaskStatus taskStatus;
    @Nullable
    public final TaskMode taskMode;
    @Nullable
    public final String chatPeerName;
    @Nullable
    public final String chatTaskTitle;
    @Nullable
    public final String chatTaskId;
    @Nullable
    public final String bizType;

    public AppNotification(@NonNull String id,
                           @NonNull String title,
                           @NonNull String body,
                           @NonNull String timeLabel,
                           boolean unread,
                           @NonNull LinkType linkType,
                           @Nullable String linkTargetId,
                           @Nullable TaskStatus taskStatus,
                           @Nullable TaskMode taskMode,
                           @Nullable String chatPeerName,
                           @Nullable String chatTaskTitle) {
        this(id, title, body, timeLabel, unread, linkType, linkTargetId,
                taskStatus, taskMode, chatPeerName, chatTaskTitle, null, null);
    }

    public AppNotification(@NonNull String id,
                           @NonNull String title,
                           @NonNull String body,
                           @NonNull String timeLabel,
                           boolean unread,
                           @NonNull LinkType linkType,
                           @Nullable String linkTargetId,
                           @Nullable TaskStatus taskStatus,
                           @Nullable TaskMode taskMode,
                           @Nullable String chatPeerName,
                           @Nullable String chatTaskTitle,
                           @Nullable String chatTaskId,
                           @Nullable String bizType) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timeLabel = timeLabel;
        this.unread = unread;
        this.linkType = linkType;
        this.linkTargetId = linkTargetId;
        this.taskStatus = taskStatus;
        this.taskMode = taskMode;
        this.chatPeerName = chatPeerName;
        this.chatTaskTitle = chatTaskTitle;
        this.chatTaskId = chatTaskId;
        this.bizType = bizType;
    }
}
