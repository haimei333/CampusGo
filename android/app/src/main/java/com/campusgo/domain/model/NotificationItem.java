package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * M03 通知列表项
 */
public class NotificationItem {

    public final String id;
    public final String title;
    public final String body;
    public final String timeLabel;
    public boolean unread;

    public NotificationItem(@NonNull String id, @NonNull String title, @NonNull String body,
                            @NonNull String timeLabel, boolean unread) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timeLabel = timeLabel;
        this.unread = unread;
    }
}
