package com.campusgo.data.remote.dto.task;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.google.gson.annotations.SerializedName;

public class PublishTaskRequest {

    @SerializedName("draftId")
    public String draftId;

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

    @SerializedName("groupTargetCount")
    public Integer groupTargetCount;
}
