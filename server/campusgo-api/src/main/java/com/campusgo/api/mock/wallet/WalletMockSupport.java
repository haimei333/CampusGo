package com.campusgo.api.mock.wallet;

import com.campusgo.api.dto.wallet.WalletTransactionDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 演示环境钱包流水：种子数据 + 运行时写入（拼单扣款/退款等）。
 */
public final class WalletMockSupport {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final AtomicLong SEQ = new AtomicLong(1000);
    private static final Map<Long, List<WalletTransactionDto>> RUNTIME = new ConcurrentHashMap<>();

    private WalletMockSupport() {
    }

    public static void recordExpense(long userId, String title, double amount) {
        prepend(userId, title, amount, WalletTransactionDto.Type.EXPENSE);
    }

    public static void recordIncome(long userId, String title, double amount) {
        prepend(userId, title, amount, WalletTransactionDto.Type.INCOME);
    }

    public static List<WalletTransactionDto> recentTransactions(long userId) {
        List<WalletTransactionDto> merged = new ArrayList<>();
        merged.addAll(RUNTIME.getOrDefault(userId, List.of()));
        merged.addAll(seedTransactions());
        return merged;
    }

    private static void prepend(long userId, String title, double amount, WalletTransactionDto.Type type) {
        WalletTransactionDto txn = WalletTransactionDto.builder()
                .id("rt-" + SEQ.incrementAndGet())
                .title(title)
                .timeLabel(LocalDateTime.now().format(TIME_FMT))
                .amount(amount)
                .type(type)
                .build();
        RUNTIME.compute(userId, (id, list) -> {
            List<WalletTransactionDto> next = new ArrayList<>();
            next.add(txn);
            if (list != null) {
                next.addAll(list);
            }
            return next;
        });
    }

    private static List<WalletTransactionDto> seedTransactions() {
        return List.of(
                WalletTransactionDto.builder()
                        .id("t1")
                        .title("任务完成 - 取快递")
                        .timeLabel("2024-03-15 14:30")
                        .amount(15.00)
                        .type(WalletTransactionDto.Type.INCOME)
                        .build(),
                WalletTransactionDto.builder()
                        .id("t2")
                        .title("充值")
                        .timeLabel("2024-03-14 10:20")
                        .amount(50.00)
                        .type(WalletTransactionDto.Type.EXPENSE)
                        .build(),
                WalletTransactionDto.builder()
                        .id("t3")
                        .title("提现")
                        .timeLabel("2024-03-13 16:45")
                        .amount(100.00)
                        .type(WalletTransactionDto.Type.EXPENSE)
                        .build(),
                WalletTransactionDto.builder()
                        .id("t4")
                        .title("任务完成 - 代买奶茶")
                        .timeLabel("2024-03-12 09:15")
                        .amount(12.00)
                        .type(WalletTransactionDto.Type.INCOME)
                        .build()
        );
    }
}
