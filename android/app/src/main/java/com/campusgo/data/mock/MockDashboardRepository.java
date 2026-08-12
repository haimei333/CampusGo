package com.campusgo.data.mock;

import com.campusgo.domain.model.DashboardStat;
import com.campusgo.domain.model.UserRole;

import java.util.ArrayList;
import java.util.List;

/**
 * D01 数据看板演示数据
 */
public final class MockDashboardRepository {

    private MockDashboardRepository() {
    }

    public static List<DashboardStat> overviewStats(UserRole role) {
        List<DashboardStat> list = new ArrayList<>();
        if (role == UserRole.RUNNER) {
            list.add(new DashboardStat("今日收入", "¥328.50", "+12.5%", true));
            list.add(new DashboardStat("今日订单", "47", "+8.3%", true));
            list.add(new DashboardStat("完成率", "94.2%", "+2.1%", true));
            list.add(new DashboardStat("评分", "4.9", "共 1,286 条评价", true));
        } else {
            list.add(new DashboardStat("已发布任务", "56", "+18.2%", true));
            list.add(new DashboardStat("本月支出", "¥1,847.00", "+15.3%", false));
            list.add(new DashboardStat("完成率", "91.8%", "+3.5%", true));
            list.add(new DashboardStat("平均响应", "8 分钟", "跑腿员接单速度", true));
        }
        return list;
    }

    public static int[] trendValues(UserRole role) {
        if (role == UserRole.RUNNER) {
            return new int[]{280, 310, 265, 340, 295, 328, 350};
        }
        return new int[]{6, 9, 5, 8, 7, 10, 11};
    }

    public static String[] trendLabels() {
        return new String[]{"07/25", "07/26", "07/27", "07/28", "07/29", "07/30", "07/31"};
    }

    public static String trendUnit(UserRole role) {
        return role == UserRole.RUNNER ? "元" : "单";
    }

    public static String trendTotal(UserRole role) {
        return role == UserRole.RUNNER ? "¥2,296.50" : "56";
    }

    public static String[][] categoryStats(UserRole role) {
        if (role == UserRole.RUNNER) {
            return new String[][]{
                    {"取快递", "42", "186"},
                    {"代买", "28", "124"},
                    {"送文件", "18", "80"},
                    {"其他", "12", "53"}
            };
        }
        return new String[][]{
                {"取快递", "38", "21"},
                {"代买", "32", "18"},
                {"送文件", "20", "11"},
                {"其他", "10", "6"}
        };
    }

    public static String categoryTotal(UserRole role) {
        return role == UserRole.RUNNER ? "443" : "56";
    }

    public static String[][] leaderboard() {
        return new String[][]{
                {"1", "张", "张伟明", "186", "金牌"},
                {"2", "李", "李晓婷", "152", "银牌"},
                {"3", "王", "王浩然", "128", "铜牌"},
                {"4", "赵", "赵雨萱", "115", "#4"},
                {"5", "陈", "陈思远", "97", "#5"}
        };
    }
}
