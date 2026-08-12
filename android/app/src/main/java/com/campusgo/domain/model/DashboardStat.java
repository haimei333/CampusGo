package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * D01 看板指标卡片
 */
public final class DashboardStat {

    @NonNull
    public final String label;
    @NonNull
    public final String value;
    @NonNull
    public final String trend;
    public final boolean trendPositive;

    public DashboardStat(@NonNull String label,
                         @NonNull String value,
                         @NonNull String trend,
                         boolean trendPositive) {
        this.label = label;
        this.value = value;
        this.trend = trend;
        this.trendPositive = trendPositive;
    }
}
