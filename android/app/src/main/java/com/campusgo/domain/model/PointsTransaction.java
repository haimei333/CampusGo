package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * 积分流水
 */
public final class PointsTransaction {

    public enum Type {
        EARN,
        SPEND
    }

    @NonNull
    public final String id;
    @NonNull
    public final String title;
    @NonNull
    public final String timeLabel;
    public final int points;
    @NonNull
    public final Type type;

    public PointsTransaction(@NonNull String id,
                             @NonNull String title,
                             @NonNull String timeLabel,
                             int points,
                             @NonNull Type type) {
        this.id = id;
        this.title = title;
        this.timeLabel = timeLabel;
        this.points = points;
        this.type = type;
    }
}
