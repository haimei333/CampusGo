package com.campusgo.data.mock;

/**
 * L02 热力图 Mock 数据
 */
public final class MockHeatmapRepository {

    private static final int[][] WEEK_GRID = {
            {0, 1, 0, 1, 0, 0, 0},
            {2, 1, 2, 2, 3, 1, 0},
            {4, 4, 4, 4, 4, 2, 1},
            {1, 2, 1, 2, 3, 1, 0},
            {3, 3, 3, 3, 4, 2, 1},
            {2, 2, 3, 2, 3, 1, 1},
    };

    private static final int[][] MONTH_GRID = {
            {1, 2, 1, 2, 2, 1, 1},
            {2, 2, 3, 2, 3, 2, 1},
            {3, 4, 4, 4, 4, 3, 2},
            {2, 2, 2, 3, 3, 2, 1},
            {3, 3, 4, 3, 4, 3, 2},
            {2, 3, 3, 3, 3, 2, 2},
    };

    private MockHeatmapRepository() {
    }

    public static int[][] grid(boolean month) {
        return month ? MONTH_GRID : WEEK_GRID;
    }

    public static int totalOrders(boolean month) {
        return month ? 742 : 186;
    }

    public static String[] timeLabels() {
        return new String[]{"8-10", "10-12", "12-14", "14-16", "16-18", "18-20"};
    }

    public static String[] dayLabels() {
        return new String[]{"一", "二", "三", "四", "五", "六", "日"};
    }
}
