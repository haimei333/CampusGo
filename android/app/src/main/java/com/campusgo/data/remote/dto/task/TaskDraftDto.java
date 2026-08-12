package com.campusgo.data.remote.dto.task;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.google.gson.annotations.SerializedName;

public class TaskDraftDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("mode")
    public TaskMode mode;

    @SerializedName("category")
    public TaskCategory category;

    @SerializedName("pickupAddress")
    public String pickupAddress;

    @SerializedName("deliveryAddress")
    public String deliveryAddress;

    @SerializedName("timeLabel")
    public String timeLabel;

    @SerializedName("rewardCent")
    public int rewardCent;

    @SerializedName("rewardYuan")
    public String rewardYuan;

    @SerializedName("updatedAt")
    public String updatedAt;
}
