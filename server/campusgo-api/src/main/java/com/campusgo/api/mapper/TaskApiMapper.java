package com.campusgo.api.mapper;

import com.campusgo.api.dto.common.PageResponse;
import com.campusgo.api.dto.task.GroupMemberDto;
import com.campusgo.api.dto.task.GroupOrderDetailDto;
import com.campusgo.api.dto.task.PublishTaskResponse;
import com.campusgo.api.dto.task.RunnerSummaryDto;
import com.campusgo.api.dto.task.TaskDetailDto;
import com.campusgo.api.dto.task.TaskDraftDto;
import com.campusgo.api.dto.task.TaskListItemDto;
import com.campusgo.application.task.TaskService;
import com.campusgo.application.task.TaskService.TaskDetailView;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.GroupOrder;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.util.MoneyUtils;

import java.util.List;

public final class TaskApiMapper {

    private TaskApiMapper() {
    }

    public static TaskService.DraftCommand toDraftCommand(com.campusgo.api.dto.task.SaveDraftRequest req) {
        return new TaskService.DraftCommand(
                req.getTitle(),
                req.getDescription(),
                req.getMode(),
                req.getCategory(),
                req.getPickupAddress(),
                req.getDeliveryAddress(),
                req.getTimeLabel(),
                req.getRewardCent()
        );
    }

    public static TaskService.PublishCommand toPublishCommand(com.campusgo.api.dto.task.PublishTaskRequest req) {
        Long draftId = parseIdOrNull(req.getDraftId());
        return new TaskService.PublishCommand(
                draftId,
                req.getTitle(),
                req.getDescription(),
                req.getMode(),
                req.getCategory(),
                req.getPickupAddress(),
                req.getDeliveryAddress(),
                req.getTimeLabel(),
                req.getRewardCent(),
                req.getGroupTargetCount()
        );
    }

    public static TaskDraftDto toDraftDto(Task task) {
        return TaskDraftDto.builder()
                .id(String.valueOf(task.getId()))
                .title(task.getTitle())
                .description(nullToEmpty(task.getDescription()))
                .mode(task.getMode())
                .category(task.getCategory())
                .pickupAddress(nullToEmpty(task.getPickupName()))
                .deliveryAddress(nullToEmpty(task.getDropoffName()))
                .timeLabel(nullToEmpty(task.getTimeLabel()))
                .rewardCent(task.getRewardCent())
                .rewardYuan(MoneyUtils.formatYuan(task.getRewardCent()))
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public static PublishTaskResponse toPublishResponse(Task task) {
        return PublishTaskResponse.builder()
                .taskId(String.valueOf(task.getId()))
                .taskNo(task.getTaskNo())
                .status(task.getStatus())
                .build();
    }

    public static TaskListItemDto toListItem(Task task) {
        return toListItem(task, null);
    }

    public static TaskListItemDto toListItem(Task task, UserRole reserveForRole) {
        return TaskListItemDto.builder()
                .id(String.valueOf(task.getId()))
                .title(task.getTitle())
                .statusLabel(statusLabel(task, reserveForRole))
                .description(nullToEmpty(task.getDescription()))
                .priceCent(task.getRewardCent())
                .priceYuan(MoneyUtils.formatYuan(task.getRewardCent()))
                .category(task.getCategory())
                .mode(task.getMode())
                .status(task.getStatus())
                .navTarget(navTarget(task))
                .reserveForRole(reserveForRole)
                .build();
    }

    public static PageResponse<TaskListItemDto> pageItems(List<TaskListItemDto> all, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int from = (safePage - 1) * safeSize;
        if (from >= all.size()) {
            return PageResponse.<TaskListItemDto>builder()
                    .list(List.of())
                    .page(safePage)
                    .pageSize(safeSize)
                    .total(all.size())
                    .build();
        }
        int to = Math.min(from + safeSize, all.size());
        return PageResponse.<TaskListItemDto>builder()
                .list(all.subList(from, to))
                .page(safePage)
                .pageSize(safeSize)
                .total(all.size())
                .build();
    }

    public static PageResponse<TaskListItemDto> page(List<Task> all, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int from = (safePage - 1) * safeSize;
        if (from >= all.size()) {
            return PageResponse.<TaskListItemDto>builder()
                    .list(List.of())
                    .page(safePage)
                    .pageSize(safeSize)
                    .total(all.size())
                    .build();
        }
        int to = Math.min(from + safeSize, all.size());
        List<TaskListItemDto> slice = all.subList(from, to).stream().map(TaskApiMapper::toListItem).toList();
        return PageResponse.<TaskListItemDto>builder()
                .list(slice)
                .page(safePage)
                .pageSize(safeSize)
                .total(all.size())
                .build();
    }

    public static TaskDetailDto toDetail(Task task) {
        return toDetail(new TaskDetailView(task, false, 0));
    }

    public static TaskDetailDto toDetail(TaskDetailView view) {
        Task task = view.task();
        RunnerSummaryDto runner = null;
        if (task.getRunnerId() != null) {
            runner = RunnerSummaryDto.builder()
                    .name(task.getRunnerName() == null ? "跑腿员" : task.getRunnerName())
                    .rating(task.getRunnerRating() == null ? 4.8f : task.getRunnerRating())
                    .creditScore(task.getRunnerCredit() == null ? 700 : task.getRunnerCredit())
                    .completedOrders(task.getRunnerCompletedOrders() == null ? 0 : task.getRunnerCompletedOrders())
                    .build();
        }
        return TaskDetailDto.builder()
                .id(String.valueOf(task.getId()))
                .title(task.getTitle())
                .categoryLabel(categoryLabel(task.getCategory()))
                .mode(task.getMode())
                .status(task.getStatus())
                .pickupAddress(nullToEmpty(task.getPickupName()))
                .deliveryAddress(nullToEmpty(task.getDropoffName()))
                .timeLabel(nullToEmpty(task.getTimeLabel()))
                .description(nullToEmpty(task.getDescription()))
                .rewardCent(task.getRewardCent())
                .rewardYuan(MoneyUtils.formatYuan(task.getRewardCent()))
                .orderNo(task.getTaskNo())
                .runner(runner)
                .reserveSlotHeld(view.reserveSlotHeld())
                .reserveHoldCount(view.reserveHoldCount())
                .build();
    }

    public static GroupOrderDetailDto toGroupDetail(GroupOrder order) {
        Task task = order.getTask();
        List<GroupMemberDto> members = order.getMembers().stream()
                .map(TaskApiMapper::toGroupMember)
                .toList();
        return GroupOrderDetailDto.builder()
                .taskId(String.valueOf(task.getId()))
                .title(task.getTitle())
                .categoryLabel(categoryLabel(task.getCategory()))
                .pickupAddress(nullToEmpty(task.getPickupName()))
                .deliverySummary(nullToEmpty(task.getDropoffName()))
                .totalReward(order.getTotalReward())
                .sharePerPerson(order.getSharePerPerson())
                .maxMembers(order.getMaxMembers())
                .joinedCount(order.getJoinedCount())
                .timeLabel(nullToEmpty(task.getTimeLabel()))
                .viewerJoined(order.isViewerJoined())
                .viewerIsCreator(order.isViewerIsCreator())
                .full(order.isFull())
                .members(members)
                .build();
    }

    private static GroupMemberDto toGroupMember(GroupMember member) {
        GroupMemberDto.Role role;
        try {
            role = GroupMemberDto.Role.valueOf(member.getRole());
        } catch (Exception e) {
            role = GroupMemberDto.Role.MEMBER;
        }
        return GroupMemberDto.builder()
                .id(member.getId() == null ? "" : String.valueOf(member.getId()))
                .name(nullToEmpty(member.getName()))
                .role(role)
                .addressSummary(nullToEmpty(member.getAddressSummary()))
                .paidAmount(member.getShareCent() / 100.0)
                .joined(role != GroupMemberDto.Role.EMPTY_SLOT)
                .build();
    }

    public static long requireId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效任务ID: " + raw);
        }
    }

    public static Long parseIdOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static TaskListItemDto.NavTarget navTarget(Task task) {
        if (task.getStatus() == TaskStatus.GROUPING || (task.getMode() == TaskMode.GROUP
                && task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.ACCEPTED
                && task.getStatus() != TaskStatus.DELIVERING && task.getStatus() != TaskStatus.CONFIRMING
                && task.getStatus() != TaskStatus.COMPLETED)) {
            return TaskListItemDto.NavTarget.T07;
        }
        if (task.getStatus() == TaskStatus.DRAFT) {
            return TaskListItemDto.NavTarget.T01;
        }
        return TaskListItemDto.NavTarget.T06;
    }

    private static String statusLabel(Task task) {
        return statusLabel(task, null);
    }

    private static String statusLabel(Task task, UserRole reserveForRole) {
        if (reserveForRole == UserRole.RUNNER && task.getStatus() == TaskStatus.RESERVING) {
            return "已占位";
        }
        if (reserveForRole == UserRole.PUBLISHER && task.getStatus() == TaskStatus.RESERVING) {
            return "预约中";
        }
        if (task.getMode() == TaskMode.EMERGENCY && task.getStatus() == TaskStatus.PENDING) {
            return "紧急";
        }
        return switch (task.getStatus()) {
            case DRAFT -> "草稿";
            case GROUPING -> "差" + Math.max(0,
                    (task.getGroupTargetCount() == null ? 3 : task.getGroupTargetCount())
                            - (task.getGroupJoinedCount() == null ? 0 : task.getGroupJoinedCount())) + "人";
            case RESERVING -> "预约中";
            case PENDING -> task.getMode() == TaskMode.GROUP ? "拼单·待抢" : "待接单";
            case ACCEPTED -> "已接单";
            case DELIVERING -> "配送中";
            case CONFIRMING -> "待确认";
            case COMPLETED -> "已完成";
            case REVIEWED -> "已评价";
            case CANCELLED -> "已取消";
        };
    }

    private static String categoryLabel(TaskCategory category) {
        if (category == null) {
            return "其他";
        }
        return switch (category) {
            case EXPRESS -> "代取快递";
            case BUY -> "代买物品";
            case ERRAND -> "代办事务";
            case OTHER -> "其他";
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
