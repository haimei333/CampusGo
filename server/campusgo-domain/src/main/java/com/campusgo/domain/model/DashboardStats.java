package com.campusgo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    List<StatItem> overviewStats;
    int[] trendValues;
    String[] trendLabels;
    String trendUnit;
    int trendTotal;
    List<CategoryItem> categoryStats;
    int categoryTotal;
    List<LeaderItem> leaderboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem {
        String label;
        Object value;
        Object trend;
        boolean trendPositive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {
        String name;
        int percent;
        int count;
        String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderItem {
        int rank;
        String avatarInitial;
        String name;
        int orderCount;
        String badge;
    }
}