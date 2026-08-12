package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * T03 任务模板
 */
public final class TaskTemplate {

    public enum Source {
        SYSTEM,
        USER
    }

    @NonNull
    public final String id;
    @NonNull
    public final Source source;
    @NonNull
    public final String name;
    @NonNull
    public final String subtitle;
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
    @NonNull
    public final String iconEmoji;

    public TaskTemplate(@NonNull String id,
                        @NonNull Source source,
                        @NonNull String name,
                        @NonNull String subtitle,
                        @NonNull String title,
                        @NonNull String description,
                        @NonNull TaskMode mode,
                        @NonNull TaskCategory category,
                        @NonNull String pickupAddress,
                        @NonNull String deliveryAddress,
                        @NonNull String timeLabel,
                        double reward,
                        @NonNull String iconEmoji) {
        this.id = id;
        this.source = source;
        this.name = name;
        this.subtitle = subtitle;
        this.title = title;
        this.description = description;
        this.mode = mode;
        this.category = category;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.timeLabel = timeLabel;
        this.reward = reward;
        this.iconEmoji = iconEmoji;
    }

    public boolean isDeletable() {
        return source == Source.USER;
    }
}
