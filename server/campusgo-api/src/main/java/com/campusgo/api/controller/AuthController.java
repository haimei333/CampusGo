package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.auth.LoginRequest;
import com.campusgo.api.dto.auth.LoginResponse;
import com.campusgo.api.dto.auth.RefreshTokenRequest;
import com.campusgo.api.dto.auth.RegisterRequest;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.auth.AuthService;
import com.campusgo.domain.model.AuthTokens;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "认证")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "注册（仅未注册手机号）")
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokens tokens = authService.register(request.getPhone(), request.getPassword());
        return ApiResponse.ok(LoginResponse.from(tokens));
    }

    @Operation(summary = "密码登录（须已注册）")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request.getPhone(), request.getPassword());
        return ApiResponse.ok(LoginResponse.from(tokens));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokens tokens = authService.refresh(request.getRefreshToken());
        return ApiResponse.ok(LoginResponse.from(tokens));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AuthUser user,
                                    @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(user.userId(), refreshToken);
        return ApiResponse.ok(null);
    }
}
