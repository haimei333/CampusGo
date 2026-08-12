package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * T06 任务详情展示模型
 */
public class TaskDetail {

    public final String id;
    public final String title;
    public final String categoryLabel;
    public TaskMode mode;
    public TaskStatus status;
    public final String pickupAddress;
    public final String deliveryAddress;
    public final String timeLabel;
    public final String description;
    public double reward;
    public final String orderNo;
    public final String runnerName;
    public final float runnerRating;
    public final int runnerCredit;
    public final int runnerCompletedOrders;

    /** 当前用户是否已占位（预约任务） */
    public boolean reserveSlotHeld;
    /** 当前预约任务占位人数 */
    public int reserveHoldCount;

    public TaskDetail(
            @NonNull String id,
            @NonNull String title,
            @NonNull String categoryLabel,
            @NonNull TaskMode mode,
            @NonNull TaskStatus status,
            @NonNull String pickupAddress,
            @NonNull String deliveryAddress,
            @NonNull String timeLabel,
            @NonNull String description,
            double reward,
            @NonNull String orderNo,
            @NonNull String runnerName,
            float runnerRating,
            int runnerCredit,
            int runnerCompletedOrders) {
        this.id = id;
        this.title = title;
        this.categoryLabel = categoryLabel;
        this.mode = mode;
        this.status = status;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.timeLabel = timeLabel;
        this.description = description;
        this.reward = reward;
        this.orderNo = orderNo;
        this.runnerName = runnerName;
        this.runnerRating = runnerRating;
        this.runnerCredit = runnerCredit;
        this.runnerCompletedOrders = runnerCompletedOrders;
    }

    public boolean isEmergency() {
        return mode == TaskMode.EMERGENCY;
    }

    public String formatReward() {
        return String.format("¥%.2f", reward);
    }
}
