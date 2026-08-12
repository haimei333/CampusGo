package com.campusgo.domain.repository;

import com.campusgo.domain.model.WalletSummary;

public interface WalletRepository {

    WalletSummary getByUserId(long userId);

    void initWallet(long userId);

    /**
     * 托管：可用余额 ↓、冻结 ↑。返回调整后可用余额。
     */
    int holdEscrow(long userId, int amountCent);

    /**
     * 托管释放（结算给跑腿员）：仅冻结 ↓，可用余额不变。返回当前可用余额。
     */
    int releaseEscrow(long userId, int amountCent);

    /**
     * 托管退回：冻结 ↓、可用余额 ↑。不计入累计收入。返回调整后可用余额。
     */
    int refundEscrow(long userId, int amountCent);

    /**
     * 任务收入入账：可用余额 ↑、累计收入 ↑。返回调整后可用余额。
     */
    int creditIncome(long userId, int amountCent);

    /**
     * 通用余额调整（如 Mock 充值）。正数计入累计收入；不足时抛业务异常。
     * 托管请用 {@link #holdEscrow}/{@link #refundEscrow}/{@link #releaseEscrow}。
     */
    int adjustBalance(long userId, int deltaCent);
}
