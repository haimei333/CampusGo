package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class PointsBalanceDto {

    @SerializedName("balance")
    public int balance;

    @SerializedName("totalEarned")
    public int totalEarned;
}