package com.campusgo.data.remote.dto.dashboard;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardStatsDto {

    @SerializedName("overviewStats")
    public List<StatItemDto> overviewStats;

    @SerializedName("trendValues")
    public List<Integer> trendValues;

    @SerializedName("trendLabels")
    public List<String> trendLabels;

    @SerializedName("trendUnit")
    public String trendUnit;

    @SerializedName("trendTotal")
    public String trendTotal;

    @SerializedName("categoryStats")
    public List<CategoryItemDto> categoryStats;

    @SerializedName("categoryTotal")
    public String categoryTotal;

    @SerializedName("leaderboard")
    public List<LeaderItemDto> leaderboard;

    public static class StatItemDto {

        @SerializedName("label")
        public String label;

        @SerializedName("value")
        public String value;

        @SerializedName("trend")
        public String trend;

        @SerializedName("trendPositive")
        public boolean trendPositive;
    }

    public static class CategoryItemDto {

        @SerializedName("name")
        public String name;

        @SerializedName("percent")
        public float percent;

        @SerializedName("count")
        public int count;

        @SerializedName("color")
        public String color;
    }

    public static class LeaderItemDto {

        @SerializedName("rank")
        public int rank;

        @SerializedName("avatarInitial")
        public String avatarInitial;

        @SerializedName("name")
        public String name;

        @SerializedName("orderCount")
        public int orderCount;

        @SerializedName("badge")
        public String badge;
    }
}