package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class PointsTransactionDto {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String type;

    @SerializedName("amount")
    public int amount;

    @SerializedName("bizType")
    public String bizType;

    @SerializedName("remark")
    public String remark;

    @SerializedName("timeLabel")
    public String timeLabel;
}