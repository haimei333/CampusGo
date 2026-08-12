package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * W03 提现记录
 */
public final class WithdrawRecord {

    public enum Status {
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @NonNull
    public final String id;
    @NonNull
    public final String title;
    @NonNull
    public final String timeLabel;
    public final double amount;
    @NonNull
    public final Status status;

    public WithdrawRecord(@NonNull String id,
                          @NonNull String title,
                          @NonNull String timeLabel,
                          double amount,
                          @NonNull Status status) {
        this.id = id;
        this.title = title;
        this.timeLabel = timeLabel;
        this.amount = amount;
        this.status = status;
    }
}
