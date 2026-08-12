package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * S03 投诉记录
 */
public final class ComplaintRecord {

    public enum Status {
        PENDING,
        DONE,
        REJECTED
    }

    @NonNull
    public final String id;
    @NonNull
    public final String type;
    @NonNull
    public final String taskTitle;
    @NonNull
    public final String description;
    @NonNull
    public final String timeLabel;
    @NonNull
    public final Status status;

    public ComplaintRecord(@NonNull String id,
                           @NonNull String type,
                           @NonNull String taskTitle,
                           @NonNull String description,
                           @NonNull String timeLabel,
                           @NonNull Status status) {
        this.id = id;
        this.type = type;
        this.taskTitle = taskTitle;
        this.description = description;
        this.timeLabel = timeLabel;
        this.status = status;
    }
}
