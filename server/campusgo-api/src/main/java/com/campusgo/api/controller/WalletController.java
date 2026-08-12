package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.wallet.TopUpRequest;
import com.campusgo.api.dto.wallet.WalletResponse;
import com.campusgo.api.dto.wallet.WalletTransactionDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.wallet.WalletService;
import com.campusgo.domain.model.WalletLedgerEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "Wallet", description = "钱包")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Shanghai"));

    private final WalletService walletService;

    @Operation(summary = "钱包余额")
    @GetMapping
    public ApiResponse<WalletResponse> getWallet(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(WalletResponse.from(walletService.getWallet(user.userId())));
    }

    @Operation(summary = "最近流水")
    @GetMapping("/transactions")
    public ApiResponse<List<WalletTransactionDto>> listTransactions(@AuthenticationPrincipal AuthUser user) {
        List<WalletTransactionDto> list = walletService.listTransactions(user.userId()).stream()
                .map(this::toDto)
                .toList();
        return ApiResponse.ok(list);
    }

    @Operation(summary = "Mock 充值")
    @PostMapping("/topup")
    public ApiResponse<WalletResponse> topup(@AuthenticationPrincipal AuthUser user,
                                             @Valid @RequestBody TopUpRequest request) {
        int cent = (int) Math.round(request.getAmount() * 100);
        walletService.topup(user.userId(), cent, "账户充值");
        return ApiResponse.ok(WalletResponse.from(walletService.getWallet(user.userId())));
    }

    private WalletTransactionDto toDto(WalletLedgerEntry entry) {
        boolean income = "IN".equalsIgnoreCase(entry.getDirection());
        String title = entry.getRemark() != null && !entry.getRemark().isBlank()
                ? entry.getRemark()
                : ledgerTitle(entry.getType());
        return WalletTransactionDto.builder()
                .id(String.valueOf(entry.getId()))
                .title(title)
                .timeLabel(entry.getCreatedAt() == null ? "" : TIME_FMT.format(entry.getCreatedAt()))
                .amount(entry.getAmountCent() / 100.0)
                .type(income ? WalletTransactionDto.Type.INCOME : WalletTransactionDto.Type.EXPENSE)
                .build();
    }

    private static String ledgerTitle(String type) {
        if (type == null) {
            return "交易";
        }
        return switch (type) {
            case "TOPUP" -> "账户充值";
            case "ESCROW_HOLD" -> "任务托管";
            case "ESCROW_RELEASE" -> "托管释放";
            case "ESCROW_REFUND" -> "托管退回";
            case "INCOME" -> "任务收入";
            case "RAISE" -> "任务加价";
            default -> type;
        };
    }
}
