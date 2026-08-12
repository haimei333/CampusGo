package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.PointsTransaction;
import com.campusgo.domain.repository.PointsTransactionRepository;
import com.campusgo.infrastructure.persistence.entity.PointsTransactionEntity;
import com.campusgo.infrastructure.persistence.jpa.PointsTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointsTransactionRepositoryImpl implements PointsTransactionRepository {

    private final PointsTransactionJpaRepository jpaRepository;

    @Override
    public PointsTransaction append(long userId, String type, int amount, int balanceAfter,
                                    String bizType, String bizId, String remark) {
        PointsTransactionEntity entity = new PointsTransactionEntity();
        entity.setUserId(userId);
        entity.setType(type);
        entity.setAmount(amount);
        entity.setBalanceAfter(balanceAfter);
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setRemark(remark);
        PointsTransactionEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    @Override
    public List<PointsTransaction> listByUser(long userId, int limit) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    private PointsTransaction toModel(PointsTransactionEntity entity) {
        return PointsTransaction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .balanceAfter(entity.getBalanceAfter())
                .bizType(entity.getBizType())
                .bizId(entity.getBizId())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}