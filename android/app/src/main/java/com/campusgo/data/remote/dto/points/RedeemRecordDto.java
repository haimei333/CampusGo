package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class RedeemRecordDto {

    @SerializedName("id")
    public String id;

    @SerializedName("productName")
    public String productName;

    @SerializedName("pointsCost")
    public int pointsCost;

    @SerializedName("address")
    public String address;

    @SerializedName("status")
    public String status;

    @SerializedName("timeLabel")
    public String timeLabel;
}