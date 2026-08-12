package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * 钱包流水（Mock）
 */
public class WalletTransaction {

    public enum Type { INCOME, EXPENSE }

    public final String id;
    public final String title;
    public final String timeLabel;
    public final double amount;
    public final Type type;

    public WalletTransaction(@NonNull String id, @NonNull String title,
                             @NonNull String timeLabel, double amount, @NonNull Type type) {
        this.id = id;
        this.title = title;
        this.timeLabel = timeLabel;
        this.amount = amount;
        this.type = type;
    }

    public String formatAmount() {
        String prefix = type == Type.INCOME ? "+" : "-";
        return prefix + String.format("¥%.2f", amount);
    }
}
