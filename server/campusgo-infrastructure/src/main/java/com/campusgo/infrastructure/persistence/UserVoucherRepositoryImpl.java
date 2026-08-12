package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.UserVoucher;
import com.campusgo.domain.repository.UserVoucherRepository;
import com.campusgo.infrastructure.persistence.entity.UserVoucherEntity;
import com.campusgo.infrastructure.persistence.jpa.UserVoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserVoucherRepositoryImpl implements UserVoucherRepository {

    private final UserVoucherJpaRepository jpaRepository;

    @Override
    public UserVoucher save(UserVoucher voucher) {
        UserVoucherEntity entity = toEntity(voucher);
        UserVoucherEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    @Override
    public Optional<UserVoucher> findById(long id) {
        return jpaRepository.findById(id).map(this::toModel);
    }

    @Override
    public Optional<UserVoucher> findByCode(String voucherCode) {
        return jpaRepository.findByVoucherCode(voucherCode).map(this::toModel);
    }

    @Override
    public List<UserVoucher> findByUserId(long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVoucher> findByUserIdAndStatus(long userId, String status) {
        return jpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void markUsed(long id) {
        jpaRepository.markUsed(id);
    }

    private UserVoucher toModel(UserVoucherEntity entity) {
        return UserVoucher.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .voucherCode(entity.getVoucherCode())
                .status(entity.getStatus())
                .expireAt(entity.getExpireAt())
                .createdAt(entity.getCreatedAt())
                .usedAt(entity.getUsedAt())
                .build();
    }

    private UserVoucherEntity toEntity(UserVoucher model) {
        UserVoucherEntity entity = new UserVoucherEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setProductId(model.getProductId());
        entity.setProductName(model.getProductName());
        entity.setVoucherCode(model.getVoucherCode());
        entity.setStatus(model.getStatus());
        entity.setExpireAt(model.getExpireAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUsedAt(model.getUsedAt());
        return entity;
    }
}
