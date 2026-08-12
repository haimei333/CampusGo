package com.campusgo.application.heatmap;

import com.campusgo.domain.model.HeatmapData;
import com.campusgo.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HeatmapServiceImpl implements HeatmapService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final TaskRepository taskRepository;

    @Override
    public HeatmapData getHeatmapData(String range) {
        Instant since = getSinceInstant(range);
        long totalOrders = taskRepository.countSince(since);

        if (totalOrders == 0) {
            return buildEmptyData(range);
        }

        List<Object[]> hourlyStats = taskRepository.hourlyPickupStats(since);

        // Build grid: rows = days of week (7), cols = 24 hours
        int[][] grid = new int[7][24];
        Map<String, Integer> pickupCounts = new LinkedHashMap<>();

        for (Object[] row : hourlyStats) {
            int hour = ((Number) row[0]).intValue();
            String pickupName = (String) row[1];
            long count = ((Number) row[2]).longValue();

            // Distribute across days (simplified: uniform distribution)
            for (int day = 0; day < 7; day++) {
                grid[day][hour] += (int) (count / 7.0);
            }

            pickupCounts.merge(pickupName, (int) count, Integer::sum);
        }

        // Build hot zones (top 5 pickup locations)
        List<HeatmapData.HotZoneItem> hotZones = pickupCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new HeatmapData.HotZoneItem(0, entry.getKey(), entry.getValue()))
                .toList();
        for (int i = 0; i < hotZones.size(); i++) {
            hotZones.get(i).setRank(i + 1);
        }

        // Build hot times (top 3 hours)
        int[] hourTotals = new int[24];
        for (int h = 0; h < 24; h++) {
            for (int d = 0; d < 7; d++) {
                hourTotals[h] += grid[d][h];
            }
        }

        List<int[]> hourWithTotal = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            hourWithTotal.add(new int[] { h, hourTotals[h] });
        }
        hourWithTotal.sort((a, b) -> Integer.compare(b[1], a[1]));

        List<HeatmapData.HotTimeItem> hotTimes = new ArrayList<>();
        for (int i = 0; i < Math.min(3, hourWithTotal.size()); i++) {
            int[] entry = hourWithTotal.get(i);
            hotTimes.add(new HeatmapData.HotTimeItem(i + 1, formatHour(entry[0]), entry[1] + "单"));
        }

        // Time labels
        String[] timeLabels;
        if ("1h".equals(range)) {
            timeLabels = generateTimeLabels(1);
        } else if ("3h".equals(range)) {
            timeLabels = generateTimeLabels(3);
        } else {
            timeLabels = generateTimeLabels(1);
        }

        String[] dayLabels = { "周一", "周二", "周三", "周四", "周五", "周六", "周日" };

        return HeatmapData.builder()
                .grid(grid)
                .totalOrders((int) totalOrders)
                .hotTimes(hotTimes)
                .hotZones(hotZones)
                .timeLabels(timeLabels)
                .dayLabels(dayLabels)
                .build();
    }

    private Instant getSinceInstant(String range) {
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        return switch (range) {
            case "1h" -> now.minusHours(1).toInstant();
            case "3h" -> now.minusHours(3).toInstant();
            case "today" -> now.toLocalDate().atStartOfDay(ZONE).toInstant();
            default -> now.minusHours(1).toInstant();
        };
    }

    private HeatmapData buildEmptyData(String range) {
        int cols = "1h".equals(range) ? 8 : ("3h".equals(range) ? 12 : 24);
        int[][] grid = new int[7][cols];
        String[] timeLabels = generateTimeLabels("1h".equals(range) ? 1 : ("3h".equals(range) ? 3 : 1));
        String[] dayLabels = { "周一", "周二", "周三", "周四", "周五", "周六", "周日" };

        return HeatmapData.builder()
                .grid(grid)
                .totalOrders(0)
                .hotTimes(List.of())
                .hotZones(List.of())
                .timeLabels(timeLabels)
                .dayLabels(dayLabels)
                .build();
    }

    private String formatHour(int hour) {
        return String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
    }

    private String[] generateTimeLabels(int hours) {
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        int startHour = now.getHour() - hours + 1;
        String[] labels = new String[hours];
        for (int i = 0; i < hours; i++) {
            int h = (startHour + i + 24) % 24;
            labels[i] = String.format("%02d:00", h);
        }
        return labels;
    }
}
