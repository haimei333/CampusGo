package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class CheckInResponseDto {

    @SerializedName("rewardPoints")
    public int rewardPoints;

    @SerializedName("newStreak")
    public int newStreak;

    @SerializedName("newBalance")
    public int newBalance;
}