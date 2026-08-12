package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupOrderDetailDto {

    @SerializedName("taskId")
    public String taskId;

    @SerializedName("title")
    public String title;

    @SerializedName("categoryLabel")
    public String categoryLabel;

    @SerializedName("pickupAddress")
    public String pickupAddress;

    @SerializedName("deliverySummary")
    public String deliverySummary;

    @SerializedName("totalReward")
    public double totalReward;

    @SerializedName("sharePerPerson")
    public double sharePerPerson;

    @SerializedName("maxMembers")
    public int maxMembers;

    @SerializedName("joinedCount")
    public int joinedCount;

    @SerializedName("timeLabel")
    public String timeLabel;

    @SerializedName("viewerJoined")
    public boolean viewerJoined;

    @SerializedName("viewerIsCreator")
    public boolean viewerIsCreator;

    @SerializedName("full")
    public boolean full;

    @SerializedName("members")
    public List<GroupMemberDto> members;
}
