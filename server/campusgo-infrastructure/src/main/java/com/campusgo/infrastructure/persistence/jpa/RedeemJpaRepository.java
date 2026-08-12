package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.RedeemRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedeemJpaRepository extends JpaRepository<RedeemRecordEntity, Long> {

    List<RedeemRecordEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}