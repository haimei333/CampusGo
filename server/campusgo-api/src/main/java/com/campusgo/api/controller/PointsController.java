package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.points.CheckInResponse;
import com.campusgo.api.dto.points.CheckInStatusResponse;
import com.campusgo.api.dto.points.MallProductDto;
import com.campusgo.api.dto.points.PointsBalanceResponse;
import com.campusgo.api.dto.points.PointsTransactionDto;
import com.campusgo.api.dto.points.RedeemRecordDto;
import com.campusgo.api.dto.points.RedeemRequest;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.points.PointsService;
import com.campusgo.application.points.PointsService.CheckInStatus;
import com.campusgo.domain.enums.MallProductCategory;
import com.campusgo.domain.model.MallProduct;
import com.campusgo.domain.model.PointsTransaction;
import com.campusgo.domain.model.RedeemRecord;
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

@Tag(name = "Points", description = "积分")
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointsController {

        private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(ZoneId.of("Asia/Shanghai"));
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withZone(ZoneId.of("Asia/Shanghai"));

        private final PointsService pointsService;

        @Operation(summary = "积分余额")
        @GetMapping
        public ApiResponse<PointsBalanceResponse> getBalance(@AuthenticationPrincipal AuthUser user) {
                int balance = pointsService.getBalance(user.userId());
                int totalEarned = 0; // 暂不返回总获取
                return ApiResponse.ok(PointsBalanceResponse.builder()
                                .balance(balance)
                                .totalEarned(totalEarned)
                                .build());
        }

        @Operation(summary = "积分流水")
        @GetMapping("/transactions")
        public ApiResponse<List<PointsTransactionDto>> listTransactions(@AuthenticationPrincipal AuthUser user) {
                List<PointsTransactionDto> list = pointsService.listTransactions(user.userId()).stream()
                                .map(this::toTransactionDto)
                                .toList();
                return ApiResponse.ok(list);
        }

        @Operation(summary = "签到")
        @PostMapping("/check-in")
        public ApiResponse<CheckInResponse> checkIn(@AuthenticationPrincipal AuthUser user) {
                int rewardPoints = pointsService.checkIn(user.userId());
                CheckInStatus status = pointsService.getCheckInStatus(user.userId());
                int balance = pointsService.getBalance(user.userId());
                return ApiResponse.ok(CheckInResponse.builder()
                                .rewardPoints(rewardPoints)
                                .newStreak(status.streak())
                                .newBalance(balance)
                                .build());
        }

        @Operation(summary = "签到状态")
        @GetMapping("/check-in/status")
        public ApiResponse<CheckInStatusResponse> checkInStatus(@AuthenticationPrincipal AuthUser user) {
                CheckInStatus status = pointsService.getCheckInStatus(user.userId());
                List<String> monthDates = status.monthDates().stream()
                                .map(d -> d.format(DATE_FMT))
                                .toList();
                return ApiResponse.ok(CheckInStatusResponse.builder()
                                .streak(status.streak())
                                .checkedInToday(status.checkedInToday())
                                .monthDates(monthDates)
                                .build());
        }

        @Operation(summary = "积分商城商品列表")
        @GetMapping("/products")
        public ApiResponse<List<MallProductDto>> listProducts(
                        @RequestParam(defaultValue = "ALL") MallProductCategory category) {
                List<MallProductDto> list = pointsService.listProducts(category).stream()
                                .map(this::toProductDto)
                                .toList();
                return ApiResponse.ok(list);
        }

        @Operation(summary = "兑换商品")
        @PostMapping("/redeem")
        public ApiResponse<RedeemRecordDto> redeem(@AuthenticationPrincipal AuthUser user,
                        @Valid @RequestBody RedeemRequest request) {
                RedeemRecord record = pointsService.redeem(user.userId(), request.getProductId(), request.getAddress());
                return ApiResponse.ok(toRedeemDto(record));
        }

        @Operation(summary = "兑换记录")
        @GetMapping("/redeems")
        public ApiResponse<List<RedeemRecordDto>> listRedeems(@AuthenticationPrincipal AuthUser user) {
                List<RedeemRecordDto> list = pointsService.listRedeems(user.userId()).stream()
                                .map(this::toRedeemDto)
                                .toList();
                return ApiResponse.ok(list);
        }

        private PointsTransactionDto toTransactionDto(PointsTransaction tx) {
                return PointsTransactionDto.builder()
                                .id(String.valueOf(tx.getId()))
                                .type(tx.getType())
                                .amount(tx.getAmount())
                                .bizType(tx.getBizType())
                                .remark(tx.getRemark())
                                .timeLabel(tx.getCreatedAt() == null ? "" : TIME_FMT.format(tx.getCreatedAt()))
                                .build();
        }

        private MallProductDto toProductDto(MallProduct product) {
                return MallProductDto.builder()
                                .id(String.valueOf(product.getId()))
                                .name(product.getName())
                                .subtitle(product.getSubtitle())
                                .category(product.getCategory())
                                .pointsCost(product.getPointsCost())
                                .stock(product.getStock())
                                .emoji(product.getEmoji())
                                .flashSale(product.isFlashSale())
                                .build();
        }

        private RedeemRecordDto toRedeemDto(RedeemRecord record) {
                return RedeemRecordDto.builder()
                                .id(String.valueOf(record.getId()))
                                .productName(record.getProductName())
                                .pointsCost(record.getPointsCost())
                                .address(record.getAddress())
                                .status(record.getStatus())
                                .timeLabel(record.getCreatedAt() == null ? "" : TIME_FMT.format(record.getCreatedAt()))
                                .build();
        }
}