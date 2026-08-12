package com.campusgo.data.remote.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.dto.task.TaskDetailDto;
import com.campusgo.data.remote.dto.task.TaskListItemDto;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskDetail;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.RecommendTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * API DTO → 现有 domain 模型，UI 层无需改动。
 */
public final class TaskDtoMapper {

    private TaskDtoMapper() {
    }

    @NonNull
    public static List<TaskListItem> toListItems(
            @Nullable List<TaskListItemDto> dtos,
            @NonNull TaskListItem.Tab tab) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<TaskListItem> result = new ArrayList<>(dtos.size());
        for (TaskListItemDto dto : dtos) {
            TaskListItem item = toListItem(dto, tab);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    @Nullable
    public static TaskListItem toListItem(@Nullable TaskListItemDto dto, @NonNull TaskListItem.Tab tab) {
        if (dto == null || dto.id == null) {
            return null;
        }
        String priceLabel = dto.priceYuan != null ? dto.priceYuan : formatYuan(dto.priceCent);
        return new TaskListItem(
                dto.id,
                tab,
                dto.reserveForRole,
                nullToEmpty(dto.title),
                nullToEmpty(dto.statusLabel),
                nullToEmpty(dto.description),
                priceLabel,
                dto.category != null ? dto.category : TaskCategory.OTHER,
                dto.mode != null ? dto.mode : TaskMode.NORMAL,
                dto.status != null ? dto.status : TaskStatus.PENDING,
                parseNavTarget(dto.navTarget));
    }

    @Nullable
    public static TaskDetail toDetail(@Nullable TaskDetailDto dto) {
        if (dto == null || dto.id == null) {
            return null;
        }
        double reward = dto.rewardCent / 100.0;
        String runnerName = "";
        float runnerRating = 0f;
        int runnerCredit = 0;
        int runnerCompleted = 0;
        if (dto.runner != null) {
            runnerName = nullToEmpty(dto.runner.name);
            runnerRating = dto.runner.rating;
            runnerCredit = dto.runner.creditScore;
            runnerCompleted = dto.runner.completedOrders;
        }
        TaskDetail detail = new TaskDetail(
                dto.id,
                nullToEmpty(dto.title),
                nullToEmpty(dto.categoryLabel),
                dto.mode != null ? dto.mode : TaskMode.NORMAL,
                dto.status != null ? dto.status : TaskStatus.PENDING,
                nullToEmpty(dto.pickupAddress),
                nullToEmpty(dto.deliveryAddress),
                nullToEmpty(dto.timeLabel),
                nullToEmpty(dto.description),
                reward,
                nullToEmpty(dto.orderNo),
                runnerName,
                runnerRating,
                runnerCredit,
                runnerCompleted);
        detail.reserveSlotHeld = dto.reserveSlotHeld;
        detail.reserveHoldCount = dto.reserveHoldCount;
        return detail;
    }

    @NonNull
    public static List<RecommendTask> toRecommendTasks(@Nullable List<TaskListItem> items, int limit) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<RecommendTask> result = new ArrayList<>(Math.min(limit, items.size()));
        for (TaskListItem item : items) {
            if (result.size() >= limit) {
                break;
            }
            result.add(toRecommendTask(item));
        }
        return result;
    }

    @NonNull
    public static RecommendTask toRecommendTask(@NonNull TaskListItem item) {
        boolean groupOrder = item.mode == TaskMode.GROUP || item.navTarget == TaskListItem.NavTarget.T07;
        boolean emergency = item.mode == TaskMode.EMERGENCY;
        RecommendTask.CardStyle style = cardStyle(item.category);
        String category = categoryLabel(item.category);
        String price = item.priceLabel != null ? item.priceLabel : "¥0.00";
        return new RecommendTask(
                item.id,
                item.title,
                category,
                emergency,
                price,
                500,
                item.statusLabel,
                50,
                60,
                item.statusLabel,
                item.description,
                defaultSizeLabel(item.category),
                groupOrder,
                style);
    }

    @NonNull
    private static RecommendTask.CardStyle cardStyle(@NonNull TaskCategory category) {
        return switch (category) {
            case BUY -> RecommendTask.CardStyle.BUY;
            case ERRAND -> RecommendTask.CardStyle.FILE;
            default -> RecommendTask.CardStyle.EXPRESS;
        };
    }

    @NonNull
    private static String categoryLabel(@NonNull TaskCategory category) {
        return switch (category) {
            case EXPRESS -> "取快递";
            case BUY -> "代买";
            case ERRAND -> "配送";
            default -> "其他";
        };
    }

    @NonNull
    private static String defaultSizeLabel(@NonNull TaskCategory category) {
        return switch (category) {
            case BUY -> "轻件";
            case ERRAND -> "文件";
            default -> "小件";
        };
    }

    @NonNull
    private static TaskListItem.NavTarget parseNavTarget(@Nullable String raw) {
        if (raw == null) {
            return TaskListItem.NavTarget.T01;
        }
        try {
            return TaskListItem.NavTarget.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return TaskListItem.NavTarget.T01;
        }
    }

    @NonNull
    private static String formatYuan(int cent) {
        return String.format("¥%.2f", cent / 100.0);
    }

    @NonNull
    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
