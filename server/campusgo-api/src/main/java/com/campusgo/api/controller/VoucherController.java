package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.points.UseVoucherRequest;
import com.campusgo.api.dto.points.UserVoucherDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.voucher.UserVoucherService;
import com.campusgo.domain.model.UserVoucher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "Voucher", description = "用户券包")
@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final UserVoucherService voucherService;

    @Operation(summary = "我的券包")
    @GetMapping
    public ApiResponse<List<UserVoucherDto>> listVouchers(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String status) {
        List<UserVoucher> vouchers;
        if (status != null && !status.isEmpty()) {
            vouchers = voucherService.getUserVouchers(user.userId()).stream()
                    .filter(v -> v.getStatus().equals(status))
                    .toList();
        } else {
            vouchers = voucherService.getUserVouchers(user.userId());
        }
        return ApiResponse.ok(vouchers.stream().map(this::toDto).toList());
    }

    @Operation(summary = "使用券")
    @PostMapping("/use")
    public ApiResponse<UserVoucherDto> useVoucher(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody UseVoucherRequest request) {
        UserVoucher voucher = voucherService.useVoucher(user.userId(), request.getVoucherCode());
        return ApiResponse.ok(toDto(voucher));
    }

    private UserVoucherDto toDto(UserVoucher v) {
        return UserVoucherDto.builder()
                .id(String.valueOf(v.getId()))
                .productName(v.getProductName())
                .voucherCode(v.getVoucherCode())
                .status(v.getStatus())
                .expireAt(v.getExpireAt() != null ? TIME_FMT.format(v.getExpireAt()) : "")
                .createdAt(v.getCreatedAt() != null ? TIME_FMT.format(v.getCreatedAt()) : "")
                .usedAt(v.getUsedAt() != null ? TIME_FMT.format(v.getUsedAt()) : null)
                .build();
    }
}
