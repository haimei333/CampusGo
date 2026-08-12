package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.common.PageResponse;
import com.campusgo.api.dto.task.CancelTaskRequest;
import com.campusgo.api.dto.task.DeliveryPhotoRequest;
import com.campusgo.api.dto.task.GroupOrderDetailDto;
import com.campusgo.api.dto.task.JoinGroupRequest;
import com.campusgo.api.dto.task.PublishTaskRequest;
import com.campusgo.api.dto.task.PublishTaskResponse;
import com.campusgo.api.dto.task.RaisePriceRequest;
import com.campusgo.api.dto.task.SaveDraftRequest;
import com.campusgo.api.dto.task.TaskDetailDto;
import com.campusgo.api.dto.task.TaskDraftDto;
import com.campusgo.api.dto.task.TaskListItemDto;
import com.campusgo.api.mapper.TaskApiMapper;
import com.campusgo.api.security.AuthUser;
import com.campusgo.api.dto.review.ReviewDto;
import com.campusgo.api.dto.review.SubmitReviewRequest;
import com.campusgo.application.review.ReviewService;
import com.campusgo.application.task.TaskService;
import com.campusgo.application.task.TaskService.ReservationEntry;
import com.campusgo.application.task.TaskService.TaskDetailView;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Task", description = "任务模块（PostgreSQL 持久化）")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campusgo.task.mock", havingValue = "false")
public class TaskController {

    private final TaskService taskService;
    private final ReviewService reviewService;

    @Operation(summary = "草稿列表")
    @GetMapping("/drafts")
    public ApiResponse<List<TaskDraftDto>> listDrafts(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(taskService.listDrafts(user.userId()).stream()
                .map(TaskApiMapper::toDraftDto)
                .toList());
    }

    @Operation(summary = "新建草稿")
    @PostMapping("/drafts")
    public ApiResponse<TaskDraftDto> createDraft(@AuthenticationPrincipal AuthUser user,
                                                 @Valid @RequestBody SaveDraftRequest request) {
        Task draft = taskService.createDraft(user.userId(), TaskApiMapper.toDraftCommand(request));
        return ApiResponse.ok(TaskApiMapper.toDraftDto(draft));
    }

    @Operation(summary = "更新草稿")
    @PutMapping("/drafts/{id}")
    public ApiResponse<TaskDraftDto> updateDraft(@AuthenticationPrincipal AuthUser user,
                                                 @PathVariable String id,
                                                 @Valid @RequestBody SaveDraftRequest request) {
        Task draft = taskService.updateDraft(user.userId(), parseId(id), TaskApiMapper.toDraftCommand(request));
        return ApiResponse.ok(TaskApiMapper.toDraftDto(draft));
    }

    @Operation(summary = "删除草稿")
    @DeleteMapping("/drafts/{id}")
    public ApiResponse<Void> deleteDraft(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        taskService.deleteDraft(user.userId(), parseId(id));
        return ApiResponse.ok(null);
    }

    @Operation(summary = "发布任务")
    @PostMapping("/publish")
    public ApiResponse<PublishTaskResponse> publish(@AuthenticationPrincipal AuthUser user,
                                                    @Valid @RequestBody PublishTaskRequest request) {
        Task task = taskService.publish(user.userId(), TaskApiMapper.toPublishCommand(request));
        return ApiResponse.ok(TaskApiMapper.toPublishResponse(task));
    }

    @Operation(summary = "任务大厅")
    @GetMapping("/hall")
    public ApiResponse<PageResponse<TaskListItemDto>> hall(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(TaskApiMapper.page(taskService.listHall(), page, pageSize));
    }

    @Operation(summary = "拼单池")
    @GetMapping("/pool")
    public ApiResponse<PageResponse<TaskListItemDto>> pool(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(TaskApiMapper.page(taskService.listPool(), page, pageSize));
    }

    @Operation(summary = "我的发布")
    @GetMapping("/mine/published")
    public ApiResponse<PageResponse<TaskListItemDto>> minePublished(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(TaskApiMapper.page(taskService.listMinePublished(user.userId()), page, pageSize));
    }

    @Operation(summary = "我的接单")
    @GetMapping("/mine/accepted")
    public ApiResponse<PageResponse<TaskListItemDto>> mineAccepted(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(TaskApiMapper.page(taskService.listMineAccepted(user.userId()), page, pageSize));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/mine/reservations")
    public ApiResponse<PageResponse<TaskListItemDto>> mineReservations(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<TaskListItemDto> all = taskService.listReservations(user.userId()).stream()
                .map(entry -> TaskApiMapper.toListItem(entry.task(), entry.reserveForRole()))
                .toList();
        return ApiResponse.ok(TaskApiMapper.pageItems(all, page, pageSize));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public ApiResponse<TaskDetailDto> detail(@AuthenticationPrincipal AuthUser user,
                                             @PathVariable String id) {
        Long viewerId = user != null ? user.userId() : null;
        TaskDetailView view = taskService.getDetail(parseId(id), viewerId);
        return ApiResponse.ok(TaskApiMapper.toDetail(view));
    }

    @Operation(summary = "拼单详情")
    @GetMapping("/{id}/group")
    public ApiResponse<GroupOrderDetailDto> groupDetail(@AuthenticationPrincipal AuthUser user,
                                                        @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toGroupDetail(taskService.getGroupDetail(parseId(id), user.userId())));
    }

    @Operation(summary = "加入拼单")
    @PostMapping("/{id}/group/join")
    public ApiResponse<GroupOrderDetailDto> joinGroup(@AuthenticationPrincipal AuthUser user,
                                                      @PathVariable String id,
                                                      @Valid @RequestBody JoinGroupRequest request) {
        return ApiResponse.ok(TaskApiMapper.toGroupDetail(
                taskService.joinGroup(parseId(id), user.userId(), request.getAddress())));
    }

    @Operation(summary = "退出拼单")
    @PostMapping("/{id}/group/leave")
    public ApiResponse<GroupOrderDetailDto> leaveGroup(@AuthenticationPrincipal AuthUser user,
                                                       @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toGroupDetail(taskService.leaveGroup(parseId(id), user.userId())));
    }

    @Operation(summary = "抢单")
    @PostMapping("/{id}/grab")
    public ApiResponse<TaskDetailDto> grab(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toDetail(
                taskService.getDetail(taskService.grab(user.userId(), parseId(id)).getId(), user.userId())));
    }

    @Operation(summary = "预约占位")
    @PostMapping("/{id}/reserve/hold")
    public ApiResponse<TaskDetailDto> holdReserve(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        long taskId = parseId(id);
        taskService.holdReserve(user.userId(), taskId);
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.getDetail(taskId, user.userId())));
    }

    @Operation(summary = "取消预约占位")
    @PostMapping("/{id}/reserve/release")
    public ApiResponse<TaskDetailDto> releaseReserve(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        long taskId = parseId(id);
        taskService.releaseReserve(user.userId(), taskId);
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.getDetail(taskId, user.userId())));
    }

    @Operation(summary = "确认预约接单")
    @PostMapping("/{id}/reserve/confirm")
    public ApiResponse<TaskDetailDto> confirmReserve(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        long taskId = parseId(id);
        taskService.confirmReserve(user.userId(), taskId);
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.getDetail(taskId, user.userId())));
    }

    @Operation(summary = "开始配送")
    @PostMapping("/{id}/deliver/start")
    public ApiResponse<TaskDetailDto> startDeliver(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.startDeliver(user.userId(), parseId(id))));
    }

    @Operation(summary = "上传送达照")
    @PostMapping("/{id}/deliver/photo")
    public ApiResponse<TaskDetailDto> uploadPhoto(@AuthenticationPrincipal AuthUser user,
                                                  @PathVariable String id,
                                                  @RequestBody(required = false) DeliveryPhotoRequest request) {
        String url = request != null ? request.getPhotoUrl() : null;
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.uploadPhoto(user.userId(), parseId(id), url)));
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirm")
    public ApiResponse<TaskDetailDto> confirm(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.confirm(user.userId(), parseId(id))));
    }

    @Operation(summary = "加价")
    @PostMapping("/{id}/raise-price")
    public ApiResponse<TaskDetailDto> raisePrice(@AuthenticationPrincipal AuthUser user,
                                                 @PathVariable String id,
                                                 @Valid @RequestBody RaisePriceRequest request) {
        return ApiResponse.ok(TaskApiMapper.toDetail(
                taskService.raisePrice(user.userId(), parseId(id), request.getAddCent())));
    }

    @Operation(summary = "转紧急")
    @PostMapping("/{id}/emergency")
    public ApiResponse<TaskDetailDto> emergency(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.toEmergency(user.userId(), parseId(id))));
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public ApiResponse<TaskDetailDto> cancel(@AuthenticationPrincipal AuthUser user,
                                             @PathVariable String id,
                                             @RequestBody(required = false) CancelTaskRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ApiResponse.ok(TaskApiMapper.toDetail(taskService.cancel(user.userId(), parseId(id), reason)));
    }

    @Operation(summary = "提交评价")
    @PostMapping("/{id}/reviews")
    public ApiResponse<ReviewDto> submitReview(@AuthenticationPrincipal AuthUser user,
                                               @PathVariable String id,
                                               @Valid @RequestBody SubmitReviewRequest request) {
        return ApiResponse.ok(ReviewDto.from(reviewService.submit(
                user.userId(),
                parseId(id),
                request.getScore(),
                request.getTags(),
                request.getContent())));
    }

    @Operation(summary = "任务评价列表")
    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ReviewDto>> listReviews(@AuthenticationPrincipal AuthUser user,
                                                    @PathVariable String id) {
        List<ReviewDto> list = reviewService.listByTask(user.userId(), parseId(id)).stream()
                .map(ReviewDto::from)
                .toList();
        return ApiResponse.ok(list);
    }

    private static long parseId(String id) {
        try {
            return TaskApiMapper.requireId(id);
        } catch (IllegalArgumentException e) {
            throw BusinessException.of(ErrorCodes.VALIDATION, e.getMessage());
        }
    }
}
