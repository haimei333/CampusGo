package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.user.CampusAuthRequest;
import com.campusgo.api.dto.user.SwitchRoleRequest;
import com.campusgo.api.dto.user.UpdateNicknameRequest;
import com.campusgo.api.dto.user.UserProfileDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "用户")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前用户资料")
    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(UserProfileDto.from(userService.getCurrentUser(user.userId())));
    }

    @Operation(summary = "更新昵称")
    @PatchMapping("/me")
    public ApiResponse<UserProfileDto> updateNickname(@AuthenticationPrincipal AuthUser user,
                                                      @Valid @RequestBody UpdateNicknameRequest request) {
        return ApiResponse.ok(UserProfileDto.from(
                userService.updateNickname(user.userId(), request.getNickname())));
    }

    @Operation(summary = "切换身份")
    @PutMapping("/me/role")
    public ApiResponse<UserProfileDto> switchRole(@AuthenticationPrincipal AuthUser user,
                                                  @Valid @RequestBody SwitchRoleRequest request) {
        return ApiResponse.ok(UserProfileDto.from(
                userService.switchRole(user.userId(), request.getActiveRole())));
    }

    @Operation(summary = "提交校园卡认证（演示自动通过）")
    @PostMapping("/me/campus-auth")
    public ApiResponse<UserProfileDto> submitCampusAuth(@AuthenticationPrincipal AuthUser user,
                                                        @Valid @RequestBody CampusAuthRequest request) {
        return ApiResponse.ok(UserProfileDto.from(
                userService.submitCampusAuth(user.userId(), request.getRealName(), request.getStudentId())));
    }
}
