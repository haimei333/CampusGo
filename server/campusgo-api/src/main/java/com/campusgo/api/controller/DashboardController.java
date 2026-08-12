package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.dashboard.DashboardResponse;
import com.campusgo.api.dto.dashboard.DashboardResponse.CategoryItemDto;
import com.campusgo.api.dto.dashboard.DashboardResponse.LeaderItemDto;
import com.campusgo.api.dto.dashboard.DashboardResponse.StatItemDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.dashboard.DashboardService;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.DashboardStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "看板")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "看板数据")
    @GetMapping("/stats")
    public ApiResponse<DashboardResponse> getStats(@AuthenticationPrincipal AuthUser user,
                                                   @RequestParam(defaultValue = "PUBLISHER") UserRole role) {
        DashboardStats stats = dashboardService.getStats(user.userId(), role);
        return ApiResponse.ok(toResponse(stats));
    }

    private static DashboardResponse toResponse(DashboardStats stats) {
        List<StatItemDto> overview = stats.getOverviewStats().stream()
                .map(s -> StatItemDto.builder()
                        .label(s.getLabel())
                        .value(s.getValue())
                        .trend(s.getTrend())
                        .trendPositive(s.isTrendPositive())
                        .build())
                .toList();

        List<CategoryItemDto> categories = stats.getCategoryStats().stream()
                .map(c -> CategoryItemDto.builder()
                        .name(c.getName())
                        .percent(c.getPercent())
                        .count(c.getCount())
                        .color(c.getColor())
                        .build())
                .toList();

        List<LeaderItemDto> leaderboard = stats.getLeaderboard().stream()
                .map(l -> LeaderItemDto.builder()
                        .rank(l.getRank())
                        .avatarInitial(l.getAvatarInitial())
                        .name(l.getName())
                        .orderCount(l.getOrderCount())
                        .badge(l.getBadge())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .overviewStats(overview)
                .trendValues(stats.getTrendValues())
                .trendLabels(stats.getTrendLabels())
                .trendUnit(stats.getTrendUnit())
                .trendTotal(stats.getTrendTotal())
                .categoryStats(categories)
                .categoryTotal(stats.getCategoryTotal())
                .leaderboard(leaderboard)
                .build();
    }
}