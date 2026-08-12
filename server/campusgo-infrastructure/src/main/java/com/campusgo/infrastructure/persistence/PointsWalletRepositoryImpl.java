package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.PointsWallet;
import com.campusgo.domain.repository.PointsWalletRepository;
import com.campusgo.infrastructure.persistence.entity.PointsWalletEntity;
import com.campusgo.infrastructure.persistence.jpa.PointsWalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointsWalletRepositoryImpl implements PointsWalletRepository {

    private final PointsWalletJpaRepository jpaRepository;

    @Override
    public Optional<PointsWallet> findByUserId(long userId) {
        return jpaRepository.findByUserId(userId).map(this::toModel);
    }

    @Override
    public void initWallet(long userId) {
        if (jpaRepository.findByUserId(userId).isPresent()) {
            return;
        }
        PointsWalletEntity entity = new PointsWalletEntity();
        entity.setUserId(userId);
        entity.setBalance(0);
        entity.setTotalEarned(0);
        jpaRepository.save(entity);
    }

    @Override
    public int addPoints(long userId, int amount) {
        requirePositive(amount);
        PointsWalletEntity entity = requireForUpdate(userId);
        entity.setBalance(entity.getBalance() + amount);
        entity.setTotalEarned(entity.getTotalEarned() + amount);
        jpaRepository.save(entity);
        return entity.getBalance();
    }

    @Override
    public int deductPoints(long userId, int amount) {
        requirePositive(amount);
        PointsWalletEntity entity = requireForUpdate(userId);
        if (entity.getBalance() < amount) {
            throw BusinessException.of(ErrorCodes.INSUFFICIENT_BALANCE, "积分不足");
        }
        entity.setBalance(entity.getBalance() - amount);
        jpaRepository.save(entity);
        return entity.getBalance();
    }

    @Override
    public PointsWallet save(PointsWallet wallet) {
        PointsWalletEntity entity = toEntity(wallet);
        PointsWalletEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    private PointsWalletEntity requireForUpdate(long userId) {
        return jpaRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "积分钱包不存在"));
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "积分数量无效");
        }
    }

    private PointsWallet toModel(PointsWalletEntity entity) {
        return PointsWallet.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .totalEarned(entity.getTotalEarned())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PointsWalletEntity toEntity(PointsWallet model) {
        PointsWalletEntity entity = new PointsWalletEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setBalance(model.getBalance());
        entity.setTotalEarned(model.getTotalEarned());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        return entity;
    }
}