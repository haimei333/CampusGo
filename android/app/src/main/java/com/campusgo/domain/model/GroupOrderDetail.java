package com.campusgo.domain.model;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * T07 拼单详情
 */
public class GroupOrderDetail {

    public final String taskId;
    public final String title;
    public final String categoryLabel;
    public final String pickupAddress;
    public final String deliverySummary;
    public final double totalReward;
    public final double sharePerPerson;
    public final int maxMembers;
    public final int joinedCount;
    public final String timeLabel;
    public final boolean viewerJoined;
    public final boolean viewerIsCreator;
    public final boolean full;
    @NonNull
    public final List<GroupMember> members;

    public GroupOrderDetail(String taskId, String title, String categoryLabel,
                            String pickupAddress, String deliverySummary,
                            double totalReward, double sharePerPerson,
                            int maxMembers, int joinedCount, String timeLabel,
                            boolean viewerJoined, boolean viewerIsCreator, boolean full,
                            @NonNull List<GroupMember> members) {
        this.taskId = taskId;
        this.title = title;
        this.categoryLabel = categoryLabel;
        this.pickupAddress = pickupAddress;
        this.deliverySummary = deliverySummary;
        this.totalReward = totalReward;
        this.sharePerPerson = sharePerPerson;
        this.maxMembers = maxMembers;
        this.joinedCount = joinedCount;
        this.timeLabel = timeLabel;
        this.viewerJoined = viewerJoined;
        this.viewerIsCreator = viewerIsCreator;
        this.full = full;
        this.members = members;
    }

    public int slotsRemaining() {
        return Math.max(0, maxMembers - joinedCount);
    }
}
