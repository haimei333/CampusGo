package com.campusgo.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * T01 发布草稿
 */
public final class PublishDraft {

    @NonNull
    public final String id;
    @NonNull
    public final String title;
    @NonNull
    public final String description;
    @NonNull
    public final TaskMode mode;
    @NonNull
    public final TaskCategory category;
    @NonNull
    public final String pickupAddress;
    @NonNull
    public final String deliveryAddress;
    @NonNull
    public final String timeLabel;
    public final double reward;
    public final long updatedAt;

    public PublishDraft(@NonNull String id,
                        @NonNull String title,
                        @NonNull String description,
                        @NonNull TaskMode mode,
                        @NonNull TaskCategory category,
                        @NonNull String pickupAddress,
                        @NonNull String deliveryAddress,
                        @NonNull String timeLabel,
                        double reward,
                        long updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mode = mode;
        this.category = category;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.timeLabel = timeLabel;
        this.reward = reward;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String listTitle() {
        return title.isEmpty() ? "未命名草稿" : title;
    }

    @NonNull
    public String listSubtitle() {
        if (!description.isEmpty()) {
            return description;
        }
        return "未提交，点进继续编辑";
    }
}
