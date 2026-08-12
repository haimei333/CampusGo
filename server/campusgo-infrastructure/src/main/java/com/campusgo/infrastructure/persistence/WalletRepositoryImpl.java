package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.WalletSummary;
import com.campusgo.domain.repository.WalletRepository;
import com.campusgo.domain.util.MoneyUtils;
import com.campusgo.infrastructure.persistence.entity.WalletEntity;
import com.campusgo.infrastructure.persistence.jpa.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    /** Demo 初始余额，与 Android SessionManager 演示值对齐（128.50 元） */
    private static final int DEMO_BALANCE_CENT = 12850;

    private final WalletJpaRepository jpaRepository;

    @Override
    public WalletSummary getByUserId(long userId) {
        return toSummary(requireWallet(userId));
    }

    @Override
    public void initWallet(long userId) {
        if (jpaRepository.findByUserId(userId).isPresent()) {
            return;
        }
        WalletEntity entity = new WalletEntity();
        entity.setUserId(userId);
        entity.setBalanceCent(DEMO_BALANCE_CENT);
        entity.setFrozenCent(0);
        entity.setTotalIncomeCent(DEMO_BALANCE_CENT);
        entity.setTotalWithdrawCent(0);
        jpaRepository.save(entity);
    }

    @Override
    public int holdEscrow(long userId, int amountCent) {
        requirePositive(amountCent);
        WalletEntity entity = requireWalletForUpdate(userId);
        if (entity.getBalanceCent() < amountCent) {
            throw BusinessException.of(ErrorCodes.INSUFFICIENT_BALANCE, "余额不足");
        }
        entity.setBalanceCent(entity.getBalanceCent() - amountCent);
        entity.setFrozenCent(entity.getFrozenCent() + amountCent);
        jpaRepository.save(entity);
        return entity.getBalanceCent();
    }

    @Override
    public int releaseEscrow(long userId, int amountCent) {
        requirePositive(amountCent);
        WalletEntity entity = requireWalletForUpdate(userId);
        if (entity.getFrozenCent() >= amountCent) {
            entity.setFrozenCent(entity.getFrozenCent() - amountCent);
        } else {
            // 旧数据：发布时只扣了余额未写入冻结，释放时清空剩余冻结即可
            entity.setFrozenCent(0);
        }
        jpaRepository.save(entity);
        return entity.getBalanceCent();
    }

    @Override
    public int refundEscrow(long userId, int amountCent) {
        requirePositive(amountCent);
        WalletEntity entity = requireWalletForUpdate(userId);
        if (entity.getFrozenCent() >= amountCent) {
            entity.setFrozenCent(entity.getFrozenCent() - amountCent);
            entity.setBalanceCent(entity.getBalanceCent() + amountCent);
        } else {
            // 旧数据：余额已扣、冻结为 0 → 全额退回可用余额
            entity.setBalanceCent(entity.getBalanceCent() + amountCent);
            entity.setFrozenCent(0);
        }
        jpaRepository.save(entity);
        return entity.getBalanceCent();
    }

    @Override
    public int creditIncome(long userId, int amountCent) {
        requirePositive(amountCent);
        WalletEntity entity = requireWalletForUpdate(userId);
        entity.setBalanceCent(entity.getBalanceCent() + amountCent);
        entity.setTotalIncomeCent(entity.getTotalIncomeCent() + amountCent);
        jpaRepository.save(entity);
        return entity.getBalanceCent();
    }

    @Override
    public int adjustBalance(long userId, int deltaCent) {
        WalletEntity entity = requireWalletForUpdate(userId);
        int next = entity.getBalanceCent() + deltaCent;
        if (next < 0) {
            throw BusinessException.of(ErrorCodes.INSUFFICIENT_BALANCE, "余额不足");
        }
        entity.setBalanceCent(next);
        if (deltaCent > 0) {
            entity.setTotalIncomeCent(entity.getTotalIncomeCent() + deltaCent);
        }
        jpaRepository.save(entity);
        return next;
    }

    private WalletEntity requireWallet(long userId) {
        return jpaRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "钱包不存在"));
    }

    private WalletEntity requireWalletForUpdate(long userId) {
        return jpaRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "钱包不存在"));
    }

    private static void requirePositive(int amountCent) {
        if (amountCent <= 0) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "金额无效");
        }
    }

    private WalletSummary toSummary(WalletEntity entity) {
        return WalletSummary.builder()
                .balanceCent(entity.getBalanceCent())
                .frozenCent(entity.getFrozenCent())
                .totalIncomeCent(entity.getTotalIncomeCent())
                .balanceYuan(MoneyUtils.formatYuan(entity.getBalanceCent()))
                .build();
    }
}
