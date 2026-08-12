package com.campusgo.data.remote.dto.heatmap;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HeatmapDataDto {

    @SerializedName("grid")
    public int[][] grid;

    @SerializedName("totalOrders")
    public int totalOrders;

    @SerializedName("hotTimes")
    public List<HotTimeDto> hotTimes;

    @SerializedName("hotZones")
    public List<HotZoneDto> hotZones;

    @SerializedName("timeLabels")
    public String[] timeLabels;

    @SerializedName("dayLabels")
    public String[] dayLabels;

    public static class HotTimeDto {

        @SerializedName("rank")
        public int rank;

        @SerializedName("timeRange")
        public String timeRange;

        @SerializedName("label")
        public String label;
    }

    public static class HotZoneDto {

        @SerializedName("rank")
        public int rank;

        @SerializedName("name")
        public String name;

        @SerializedName("orderCount")
        public int orderCount;
    }
}