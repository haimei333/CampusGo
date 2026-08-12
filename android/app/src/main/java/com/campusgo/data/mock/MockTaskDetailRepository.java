package com.campusgo.data.mock;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskDetail;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;

/**
 * T06 演示任务详情
 */
public final class MockTaskDetailRepository {

    private MockTaskDetailRepository() {
    }

    @NonNull
    public static TaskDetail fromListItem(@NonNull TaskListItem item) {
        String category = "done1".equals(item.id) ? "中通快递" : categoryLabel(item.category);
        return new TaskDetail(
                item.id,
                item.title,
                category,
                item.mode,
                item.status,
                defaultPickup(item),
                defaultDelivery(item),
                defaultTime(item),
                item.description,
                parsePrice(item.priceLabel),
                "20240725" + item.id.hashCode(),
                "张同学",
                4.8f,
                720,
                32);
    }

    @NonNull
    public static TaskDetail defaultTask() {
        return new TaskDetail(
                "demo",
                "取快递 - 中通快递",
                "代取快递",
                TaskMode.NORMAL,
                TaskStatus.PENDING,
                "北京大学 南门快递站",
                "北京大学 38号楼",
                "尽快送达",
                "小件快递，取件码 1234",
                15.0,
                "20240725001",
                "张同学",
                4.8f,
                720,
                32);
    }

    @Nullable
    public static TaskDetail findById(@NonNull Context context, @NonNull String id) {
        for (TaskListItem item : MockTaskRepository.all(context)) {
            if (item.id.equals(id)) {
                return fromListItem(item);
            }
        }
        if ("demo".equals(id)) {
            return defaultTask();
        }
        return null;
    }

    @NonNull
    private static String categoryLabel(@NonNull TaskCategory category) {
        switch (category) {
            case EXPRESS:
                return "代取快递";
            case BUY:
                return "代买物品";
            case ERRAND:
                return "代办事务";
            default:
                return "其他";
        }
    }

    @NonNull
    private static String defaultPickup(@NonNull TaskListItem item) {
        if (item.title.contains("菜鸟") || item.title.contains("快递")) {
            return "菜鸟驿站 · 东门";
        }
        if (item.title.contains("食堂")) {
            return "二食堂";
        }
        return "图书馆";
    }

    @NonNull
    private static String defaultDelivery(@NonNull TaskListItem item) {
        if (item.title.contains("宿舍") || item.title.contains("快递")) {
            return "宿舍楼 5栋";
        }
        if (item.title.contains("行政")) {
            return "行政楼 3层";
        }
        return "教学楼 A座";
    }

    @NonNull
    private static String defaultTime(@NonNull TaskListItem item) {
        if (item.mode == TaskMode.RESERVE) {
            return "明日 18:00";
        }
        return "尽快送达";
    }

    private static double parsePrice(@Nullable String priceLabel) {
        if (priceLabel == null || priceLabel.equals("—")) {
            return 0;
        }
        try {
            return Double.parseDouble(priceLabel.replace("¥", "").trim());
        } catch (NumberFormatException e) {
            return 15.0;
        }
    }
}
