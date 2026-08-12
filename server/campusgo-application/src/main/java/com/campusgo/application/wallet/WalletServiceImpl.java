package com.campusgo.application.wallet;

import com.campusgo.domain.model.WalletLedgerEntry;
import com.campusgo.domain.model.WalletSummary;
import com.campusgo.domain.repository.WalletLedgerRepository;
import com.campusgo.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletLedgerRepository walletLedgerRepository;

    @Override
    public WalletSummary getWallet(long userId) {
        return walletRepository.getByUserId(userId);
    }

    @Override
    public List<WalletLedgerEntry> listTransactions(long userId) {
        return walletLedgerRepository.listByUser(userId, 50);
    }

    @Override
    @Transactional
    public int hold(long userId, int amountCent, Long taskId, String remark) {
        int after = walletRepository.holdEscrow(userId, amountCent);
        walletLedgerRepository.append(userId, "ESCROW_HOLD", amountCent, "OUT", after, taskId, remark);
        return after;
    }

    @Override
    @Transactional
    public int holdRaise(long userId, int amountCent, Long taskId, String remark) {
        int after = walletRepository.holdEscrow(userId, amountCent);
        walletLedgerRepository.append(userId, "RAISE", amountCent, "OUT", after, taskId, remark);
        return after;
    }

    @Override
    @Transactional
    public int releaseEscrow(long userId, int amountCent, Long taskId, String remark) {
        int after = walletRepository.releaseEscrow(userId, amountCent);
        // 可用余额不变；流水记 OUT 表示托管离开账户体系划转给跑腿员
        walletLedgerRepository.append(userId, "ESCROW_RELEASE", amountCent, "OUT", after, taskId, remark);
        return after;
    }

    @Override
    @Transactional
    public int refundEscrow(long userId, int amountCent, Long taskId, String remark) {
        int after = walletRepository.refundEscrow(userId, amountCent);
        walletLedgerRepository.append(userId, "ESCROW_REFUND", amountCent, "IN", after, taskId, remark);
        return after;
    }

    @Override
    @Transactional
    public int creditIncome(long userId, int amountCent, Long taskId, String remark) {
        int after = walletRepository.creditIncome(userId, amountCent);
        walletLedgerRepository.append(userId, "INCOME", amountCent, "IN", after, taskId, remark);
        return after;
    }

    @Override
    @Transactional
    public int topup(long userId, int amountCent, String remark) {
        if (amountCent <= 0) {
            throw com.campusgo.domain.exception.BusinessException.of(
                    com.campusgo.domain.exception.ErrorCodes.VALIDATION, "充值金额无效");
        }
        int after = walletRepository.adjustBalance(userId, amountCent);
        walletLedgerRepository.append(userId, "TOPUP", amountCent, "IN", after, null,
                remark != null ? remark : "账户充值");
        return after;
    }
}
