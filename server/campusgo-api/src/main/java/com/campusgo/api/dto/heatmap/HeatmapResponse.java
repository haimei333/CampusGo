package com.campusgo.api.dto.heatmap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "热力图数据")
public class HeatmapResponse {

    int[][] grid;
    int totalOrders;
    List<HotTimeDto> hotTimes;
    List<HotZoneDto> hotZones;
    String[] timeLabels;
    String[] dayLabels;

    @Value
    @Builder
    public static class HotTimeDto {
        int rank;
        String timeRange;
        String label;
    }

    @Value
    @Builder
    public static class HotZoneDto {
        int rank;
        String name;
        int orderCount;
    }
}