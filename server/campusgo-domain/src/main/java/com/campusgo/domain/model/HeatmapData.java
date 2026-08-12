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
public class HeatmapData {

    int[][] grid;
    int totalOrders;
    List<HotTimeItem> hotTimes;
    List<HotZoneItem> hotZones;
    String[] timeLabels;
    String[] dayLabels;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotTimeItem {
        int rank;
        String timeRange;
        String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotZoneItem {
        int rank;
        String name;
        int orderCount;
    }
}