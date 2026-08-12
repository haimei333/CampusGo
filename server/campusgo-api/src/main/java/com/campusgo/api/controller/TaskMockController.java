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
import com.campusgo.api.mock.task.TaskMockService;
import com.campusgo.api.security.AuthUser;
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

/**
 * Task 模块 Mock API — 供 Android 联调，后续替换为真实 TaskService 实现。
 */
@Tag(name = "Task (Mock)", description = "任务模块 Mock 接口，契约见 server/openapi/task-api.yaml")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campusgo.task.mock", havingValue = "true", matchIfMissing = true)
public class TaskMockController {

    private final TaskMockService taskMockService;

    @Operation(summary = "草稿列表")
    @GetMapping("/drafts")
    public ApiResponse<List<TaskDraftDto>> listDrafts(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(taskMockService.listDrafts(user.userId()));
    }

    @Operation(summary = "新建草稿")
    @PostMapping("/drafts")
    public ApiResponse<TaskDraftDto> createDraft(@AuthenticationPrincipal AuthUser user,
                                                 @Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(taskMockService.createDraft(user.userId(), request));
    }

    @Operation(summary = "更新草稿")
    @PutMapping("/drafts/{id}")
    public ApiResponse<TaskDraftDto> updateDraft(@AuthenticationPrincipal AuthUser user,
                                                 @PathVariable String id,
                                                 @Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(taskMockService.updateDraft(user.userId(), id, request));
    }

    @Operation(summary = "删除草稿")
    @DeleteMapping("/drafts/{id}")
    public ApiResponse<Void> deleteDraft(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        taskMockService.deleteDraft(user.userId(), id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "发布任务")
    @PostMapping("/publish")
    public ApiResponse<PublishTaskResponse> publish(@AuthenticationPrincipal AuthUser user,
                                                    @Valid @RequestBody PublishTaskRequest request) {
        return ApiResponse.ok(taskMockService.publish(user.userId(), request));
    }

    @Operation(summary = "任务大厅")
    @GetMapping("/hall")
    public ApiResponse<PageResponse<TaskListItemDto>> hall(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(taskMockService.listHall(page, pageSize));
    }

    @Operation(summary = "拼单池")
    @GetMapping("/pool")
    public ApiResponse<PageResponse<TaskListItemDto>> pool(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(taskMockService.listPool(page, pageSize));
    }

    @Operation(summary = "我的发布")
    @GetMapping("/mine/published")
    public ApiResponse<PageResponse<TaskListItemDto>> minePublished(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(taskMockService.listMinePublished(user.userId(), page, pageSize));
    }

    @Operation(summary = "我的接单")
    @GetMapping("/mine/accepted")
    public ApiResponse<PageResponse<TaskListItemDto>> mineAccepted(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(taskMockService.listMineAccepted(user.userId(), page, pageSize));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/mine/reservations")
    public ApiResponse<PageResponse<TaskListItemDto>> mineReservations(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(taskMockService.listMineReservations(user.userId(), page, pageSize));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public ApiResponse<TaskDetailDto> detail(@PathVariable String id) {
        return ApiResponse.ok(taskMockService.getDetail(id));
    }

    @Operation(summary = "拼单详情")
    @GetMapping("/{id}/group")
    public ApiResponse<GroupOrderDetailDto> groupDetail(@AuthenticationPrincipal AuthUser user,
                                                        @PathVariable String id) {
        return ApiResponse.ok(taskMockService.getGroupDetail(id, user.userId()));
    }

    @Operation(summary = "加入拼单")
    @PostMapping("/{id}/group/join")
    public ApiResponse<GroupOrderDetailDto> joinGroup(@AuthenticationPrincipal AuthUser user,
                                                      @PathVariable String id,
                                                      @Valid @RequestBody JoinGroupRequest request) {
        return ApiResponse.ok(taskMockService.joinGroup(id, user.userId(), request));
    }

    @Operation(summary = "退出拼单")
    @PostMapping("/{id}/group/leave")
    public ApiResponse<GroupOrderDetailDto> leaveGroup(@AuthenticationPrincipal AuthUser user,
                                                       @PathVariable String id) {
        return ApiResponse.ok(taskMockService.leaveGroup(id, user.userId()));
    }

    @Operation(summary = "抢单")
    @PostMapping("/{id}/grab")
    public ApiResponse<TaskDetailDto> grab(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(taskMockService.grab(user.userId(), id));
    }

    @Operation(summary = "开始配送")
    @PostMapping("/{id}/deliver/start")
    public ApiResponse<TaskDetailDto> startDeliver(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(taskMockService.startDeliver(user.userId(), id));
    }

    @Operation(summary = "上传送达照")
    @PostMapping("/{id}/deliver/photo")
    public ApiResponse<TaskDetailDto> uploadPhoto(@AuthenticationPrincipal AuthUser user,
                                                  @PathVariable String id,
                                                  @RequestBody(required = false) DeliveryPhotoRequest request) {
        String url = request != null ? request.getPhotoUrl() : null;
        return ApiResponse.ok(taskMockService.uploadPhoto(user.userId(), id, url));
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirm")
    public ApiResponse<TaskDetailDto> confirm(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(taskMockService.confirm(user.userId(), id));
    }

    @Operation(summary = "加价")
    @PostMapping("/{id}/raise-price")
    public ApiResponse<TaskDetailDto> raisePrice(@AuthenticationPrincipal AuthUser user,
                                                 @PathVariable String id,
                                                 @Valid @RequestBody RaisePriceRequest request) {
        return ApiResponse.ok(taskMockService.raisePrice(user.userId(), id, request.getAddCent()));
    }

    @Operation(summary = "转紧急")
    @PostMapping("/{id}/emergency")
    public ApiResponse<TaskDetailDto> emergency(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return ApiResponse.ok(taskMockService.toEmergency(user.userId(), id));
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public ApiResponse<TaskDetailDto> cancel(@AuthenticationPrincipal AuthUser user,
                                             @PathVariable String id,
                                             @RequestBody(required = false) CancelTaskRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ApiResponse.ok(taskMockService.cancel(user.userId(), id, reason));
    }
}
