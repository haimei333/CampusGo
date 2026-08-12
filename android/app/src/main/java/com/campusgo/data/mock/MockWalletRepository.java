package com.campusgo.data.mock;

import com.campusgo.domain.model.WalletTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * W01 钱包演示数据
 */
public final class MockWalletRepository {

    private MockWalletRepository() {
    }

    public static List<WalletTransaction> recentTransactions() {
        List<WalletTransaction> list = new ArrayList<>();
        list.add(new WalletTransaction("t1", "任务完成 - 取快递",
                "2024-03-15 14:30", 15.00, WalletTransaction.Type.INCOME));
        list.add(new WalletTransaction("t2", "充值",
                "2024-03-14 10:20", 50.00, WalletTransaction.Type.EXPENSE));
        list.add(new WalletTransaction("t3", "提现",
                "2024-03-13 16:45", 100.00, WalletTransaction.Type.EXPENSE));
        list.add(new WalletTransaction("t4", "任务完成 - 代买奶茶",
                "2024-03-12 09:15", 12.00, WalletTransaction.Type.INCOME));
        return list;
    }
}
