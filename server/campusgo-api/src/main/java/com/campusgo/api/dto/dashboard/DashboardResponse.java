package com.campusgo.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "看板数据")
public class DashboardResponse {

    List<StatItemDto> overviewStats;
    int[] trendValues;
    String[] trendLabels;
    String trendUnit;
    int trendTotal;
    List<CategoryItemDto> categoryStats;
    int categoryTotal;
    List<LeaderItemDto> leaderboard;

    @Value
    @Builder
    public static class StatItemDto {
        String label;
        Object value;
        Object trend;
        boolean trendPositive;
    }

    @Value
    @Builder
    public static class CategoryItemDto {
        String name;
        int percent;
        int count;
        String color;
    }

    @Value
    @Builder
    public static class LeaderItemDto {
        int rank;
        String avatarInitial;
        String name;
        int orderCount;
        String badge;
    }
}