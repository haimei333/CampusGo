package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.notification.NotificationDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification", description = "系统通知")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping
    public ApiResponse<List<NotificationDto>> list(@AuthenticationPrincipal AuthUser user) {
        List<NotificationDto> list = notificationService.list(user.userId()).stream()
                .map(NotificationDto::from)
                .toList();
        return ApiResponse.ok(list);
    }

    @Operation(summary = "未读数")
    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(notificationService.unreadCount(user.userId()));
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@AuthenticationPrincipal AuthUser user,
                                      @PathVariable("id") long id) {
        notificationService.markRead(user.userId(), id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "全部已读")
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal AuthUser user) {
        notificationService.markAllRead(user.userId());
        return ApiResponse.ok(null);
    }
}
