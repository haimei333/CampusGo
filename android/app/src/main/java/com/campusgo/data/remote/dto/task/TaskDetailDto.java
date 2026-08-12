package com.campusgo.data.remote.dto.task;

import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.google.gson.annotations.SerializedName;

public class TaskDetailDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("categoryLabel")
    public String categoryLabel;

    @SerializedName("mode")
    public TaskMode mode;

    @SerializedName("status")
    public TaskStatus status;

    @SerializedName("pickupAddress")
    public String pickupAddress;

    @SerializedName("deliveryAddress")
    public String deliveryAddress;

    @SerializedName("timeLabel")
    public String timeLabel;

    @SerializedName("description")
    public String description;

    @SerializedName("rewardCent")
    public int rewardCent;

    @SerializedName("rewardYuan")
    public String rewardYuan;

    @SerializedName("orderNo")
    public String orderNo;

    @SerializedName("runner")
    public RunnerSummaryDto runner;

    @SerializedName("reserveSlotHeld")
    public boolean reserveSlotHeld;

    @SerializedName("reserveHoldCount")
    public int reserveHoldCount;
}
