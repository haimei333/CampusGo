package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class RunnerSummaryDto {

    @SerializedName("name")
    public String name;

    @SerializedName("rating")
    public float rating;

    @SerializedName("creditScore")
    public int creditScore;

    @SerializedName("completedOrders")
    public int completedOrders;
}
