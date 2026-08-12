package com.campusgo.application.dashboard;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.DashboardStats;
import com.campusgo.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;

    @Override
    public DashboardStats getStats(long userId, UserRole role) {
        if (role == UserRole.PUBLISHER) {
            return publisherStats(userId);
        }
        return runnerStats(userId);
    }

    private DashboardStats publisherStats(long userId) {
        // Status counts
        List<Object[]> statusRows = taskRepository.countByStatusForPublisher(userId);
        Map<TaskStatus, Long> statusMap = new HashMap<>();
        for (Object[] row : statusRows) {
            statusMap.put((TaskStatus) row[0], ((Number) row[1]).longValue());
        }

        long published = statusRows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        long inProgress = statusMap.getOrDefault(TaskStatus.ACCEPTED, 0L)
                + statusMap.getOrDefault(TaskStatus.DELIVERING, 0L)
                + statusMap.getOrDefault(TaskStatus.CONFIRMING, 0L);
        long completed = statusMap.getOrDefault(TaskStatus.COMPLETED, 0L)
                + statusMap.getOrDefault(TaskStatus.REVIEWED, 0L);
        long cancelled = statusMap.getOrDefault(TaskStatus.CANCELLED, 0L);

        // Category counts
        List<Object[]> catRows = taskRepository.countByCategoryForPublisher(userId);
        Map<TaskCategory, Long> catMap = new HashMap<>();
        for (Object[] row : catRows) {
            catMap.put((TaskCategory) row[0], ((Number) row[1]).longValue());
        }

        // Monthly trend
        List<Object[]> trendRows = taskRepository.monthlyTrendForPublisher(userId);
        int[] trendValues = new int[12];
        String[] trendLabels = { "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月" };
        for (Object[] row : trendRows) {
            int month = ((Number) row[0]).intValue() - 1;
            if (month >= 0 && month < 12) {
                trendValues[month] = ((Number) row[1]).intValue();
            }
        }
        int trendTotal = (int) published;

        // Leaderboard
        List<Object[]> topRows = taskRepository.topRunners();
        List<DashboardStats.LeaderItem> leaderboard = new ArrayList<>();
        String[] badges = { "金牌", "银牌", "铜牌" };
        for (int i = 0; i < Math.min(3, topRows.size()); i++) {
            Object[] row = topRows.get(i);
            String nickname = (String) row[1];
            String initial = nickname.isEmpty() ? "?" : nickname.substring(0, 1);
            leaderboard.add(
                    new DashboardStats.LeaderItem(i + 1, initial, nickname, ((Number) row[2]).intValue(), badges[i]));
        }

        return DashboardStats.builder()
                .overviewStats(List.of(
                        new DashboardStats.StatItem("发布任务", (int) published, 0, true),
                        new DashboardStats.StatItem("进行中", (int) inProgress, 0, true),
                        new DashboardStats.StatItem("已完成", (int) completed, 0, true),
                        new DashboardStats.StatItem("取消", (int) cancelled, 0, true)))
                .trendValues(trendValues)
                .trendLabels(trendLabels)
                .trendUnit("单")
                .trendTotal(trendTotal)
                .categoryStats(buildCategoryItems(catMap, (int) published))
                .categoryTotal((int) published)
                .leaderboard(leaderboard)
                .build();
    }

    private DashboardStats runnerStats(long userId) {
        // Status counts
        List<Object[]> statusRows = taskRepository.countByStatusForRunner(userId);
        Map<TaskStatus, Long> statusMap = new HashMap<>();
        for (Object[] row : statusRows) {
            statusMap.put((TaskStatus) row[0], ((Number) row[1]).longValue());
        }

        long accepted = statusRows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        long delivering = statusMap.getOrDefault(TaskStatus.DELIVERING, 0L)
                + statusMap.getOrDefault(TaskStatus.CONFIRMING, 0L);
        long completed = statusMap.getOrDefault(TaskStatus.COMPLETED, 0L)
                + statusMap.getOrDefault(TaskStatus.REVIEWED, 0L);

        // Category counts
        List<Object[]> catRows = taskRepository.countByCategoryForRunner(userId);
        Map<TaskCategory, Long> catMap = new HashMap<>();
        for (Object[] row : catRows) {
            catMap.put((TaskCategory) row[0], ((Number) row[1]).longValue());
        }

        // Monthly trend
        List<Object[]> trendRows = taskRepository.monthlyTrendForRunner(userId);
        int[] trendValues = new int[12];
        String[] trendLabels = { "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月" };
        for (Object[] row : trendRows) {
            int month = ((Number) row[0]).intValue() - 1;
            if (month >= 0 && month < 12) {
                trendValues[month] = ((Number) row[1]).intValue();
            }
        }
        int trendTotal = (int) accepted;

        // Leaderboard
        List<Object[]> topRows = taskRepository.topRunners();
        List<DashboardStats.LeaderItem> leaderboard = new ArrayList<>();
        String[] badges = { "金牌", "银牌", "铜牌" };
        for (int i = 0; i < Math.min(3, topRows.size()); i++) {
            Object[] row = topRows.get(i);
            String nickname = (String) row[1];
            String initial = nickname.isEmpty() ? "?" : nickname.substring(0, 1);
            leaderboard.add(
                    new DashboardStats.LeaderItem(i + 1, initial, nickname, ((Number) row[2]).intValue(), badges[i]));
        }

        return DashboardStats.builder()
                .overviewStats(List.of(
                        new DashboardStats.StatItem("接单", (int) accepted, 0, true),
                        new DashboardStats.StatItem("配送中", (int) delivering, 0, true),
                        new DashboardStats.StatItem("已完成", (int) completed, 0, true),
                        new DashboardStats.StatItem("评价", "—", 0, true)))
                .trendValues(trendValues)
                .trendLabels(trendLabels)
                .trendUnit("单")
                .trendTotal(trendTotal)
                .categoryStats(buildCategoryItems(catMap, (int) accepted))
                .categoryTotal((int) accepted)
                .leaderboard(leaderboard)
                .build();
    }

    private static List<DashboardStats.CategoryItem> buildCategoryItems(Map<TaskCategory, Long> catMap, int total) {
        String[] names = { "取快递", "买饭", "打印资料", "其他" };
        TaskCategory[] cats = { TaskCategory.EXPRESS, TaskCategory.BUY, TaskCategory.ERRAND, TaskCategory.OTHER };
        String[] colors = { "#4F86C6", "#6ABF69", "#F5A623", "#E57373" };

        List<DashboardStats.CategoryItem> items = new ArrayList<>();
        for (int i = 0; i < cats.length; i++) {
            long count = catMap.getOrDefault(cats[i], 0L);
            int percent = total > 0 ? (int) Math.round(count * 100.0 / total) : 0;
            items.add(new DashboardStats.CategoryItem(names[i], percent, (int) count, colors[i]));
        }
        return items;
    }
}
