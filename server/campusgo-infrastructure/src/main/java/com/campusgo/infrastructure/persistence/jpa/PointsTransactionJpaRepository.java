package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.PointsTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointsTransactionJpaRepository extends JpaRepository<PointsTransactionEntity, Long> {

    List<PointsTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PointsTransactionEntity> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}