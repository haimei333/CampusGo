package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.heatmap.HeatmapResponse;
import com.campusgo.api.dto.heatmap.HeatmapResponse.HotTimeDto;
import com.campusgo.api.dto.heatmap.HeatmapResponse.HotZoneDto;
import com.campusgo.application.heatmap.HeatmapService;
import com.campusgo.domain.model.HeatmapData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Heatmap", description = "热力图")
@RestController
@RequestMapping("/api/v1/heatmap")
@RequiredArgsConstructor
public class HeatmapController {

    private final HeatmapService heatmapService;

    @Operation(summary = "热力图数据")
    @GetMapping("/data")
    public ApiResponse<HeatmapResponse> getHeatmapData(@RequestParam(defaultValue = "1h") String range) {
        HeatmapData data = heatmapService.getHeatmapData(range);
        return ApiResponse.ok(toResponse(data));
    }

    private static HeatmapResponse toResponse(HeatmapData data) {
        List<HotTimeDto> hotTimes = data.getHotTimes().stream()
                .map(h -> HotTimeDto.builder()
                        .rank(h.getRank())
                        .timeRange(h.getTimeRange())
                        .label(h.getLabel())
                        .build())
                .toList();

        List<HotZoneDto> hotZones = data.getHotZones().stream()
                .map(h -> HotZoneDto.builder()
                        .rank(h.getRank())
                        .name(h.getName())
                        .orderCount(h.getOrderCount())
                        .build())
                .toList();

        return HeatmapResponse.builder()
                .grid(data.getGrid())
                .totalOrders(data.getTotalOrders())
                .hotTimes(hotTimes)
                .hotZones(hotZones)
                .timeLabels(data.getTimeLabels())
                .dayLabels(data.getDayLabels())
                .build();
    }
}