package com.campusgo.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * M02 任务列表项（Mock 数据 / 后续接 API）
 */
public class TaskListItem {

    public enum Tab {
        HALL,
        MINE_PUBLISH,
        MINE_TAKE,
        POOL,
        RESERVE
    }

    public enum NavTarget {
        T01,
        T06,
        T07
    }

    public final String id;
    public final Tab tab;
    @Nullable
    public final UserRole reserveForRole;
    public final String title;
    public final String statusLabel;
    public final String description;
    @Nullable
    public final String priceLabel;
    public final TaskCategory category;
    public final TaskMode mode;
    public final TaskStatus status;
    public final NavTarget navTarget;

    public TaskListItem(
            @NonNull String id,
            @NonNull Tab tab,
            @Nullable UserRole reserveForRole,
            @NonNull String title,
            @NonNull String statusLabel,
            @NonNull String description,
            @Nullable String priceLabel,
            @NonNull TaskCategory category,
            @NonNull TaskMode mode,
            @NonNull TaskStatus status,
            @NonNull NavTarget navTarget) {
        this.id = id;
        this.tab = tab;
        this.reserveForRole = reserveForRole;
        this.title = title;
        this.statusLabel = statusLabel;
        this.description = description;
        this.priceLabel = priceLabel;
        this.category = category;
        this.mode = mode;
        this.status = status;
        this.navTarget = navTarget;
    }
}
