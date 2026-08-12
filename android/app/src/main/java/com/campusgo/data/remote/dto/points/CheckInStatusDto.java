package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckInStatusDto {

    @SerializedName("streak")
    public int streak;

    @SerializedName("checkedInToday")
    public boolean checkedInToday;

    @SerializedName("monthDates")
    public List<String> monthDates;
}