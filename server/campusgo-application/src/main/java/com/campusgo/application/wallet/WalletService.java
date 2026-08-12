package com.campusgo.application.wallet;

import com.campusgo.domain.model.WalletLedgerEntry;
import com.campusgo.domain.model.WalletSummary;

import java.util.List;

public interface WalletService {

    WalletSummary getWallet(long userId);

    List<WalletLedgerEntry> listTransactions(long userId);

    /** 托管扣款：balance↓ frozen↑，写 ESCROW_HOLD。返回可用余额。 */
    int hold(long userId, int amountCent, Long taskId, String remark);

    /** 加价补托管：同 hold，流水类型 RAISE。 */
    int holdRaise(long userId, int amountCent, Long taskId, String remark);

    /** 确认结算释放托管：frozen↓，写 ESCROW_RELEASE。返回可用余额（不变）。 */
    int releaseEscrow(long userId, int amountCent, Long taskId, String remark);

    /** 取消退托管：frozen↓ balance↑，写 ESCROW_REFUND。返回可用余额。 */
    int refundEscrow(long userId, int amountCent, Long taskId, String remark);

    /** 跑腿员入账：balance↑，写 INCOME。返回可用余额。 */
    int creditIncome(long userId, int amountCent, Long taskId, String remark);

    /** Mock 充值：balance↑，写 TOPUP。返回可用余额。 */
    int topup(long userId, int amountCent, String remark);
}
