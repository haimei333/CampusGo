package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.RedeemRecord;
import com.campusgo.domain.repository.RedeemRepository;
import com.campusgo.infrastructure.persistence.entity.RedeemRecordEntity;
import com.campusgo.infrastructure.persistence.jpa.RedeemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedeemRepositoryImpl implements RedeemRepository {

    private final RedeemJpaRepository jpaRepository;

    @Override
    public RedeemRecord save(RedeemRecord record) {
        RedeemRecordEntity entity = toEntity(record);
        RedeemRecordEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    @Override
    public List<RedeemRecord> findByUserId(long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    private RedeemRecord toModel(RedeemRecordEntity entity) {
        return RedeemRecord.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .pointsCost(entity.getPointsCost())
                .address(entity.getAddress())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private RedeemRecordEntity toEntity(RedeemRecord model) {
        RedeemRecordEntity entity = new RedeemRecordEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setProductId(model.getProductId());
        entity.setProductName(model.getProductName());
        entity.setPointsCost(model.getPointsCost());
        entity.setAddress(model.getAddress());
        entity.setStatus(model.getStatus());
        entity.setCreatedAt(model.getCreatedAt());
        return entity;
    }
}