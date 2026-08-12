package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.PointsWalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointsWalletJpaRepository extends JpaRepository<PointsWalletEntity, Long> {

    Optional<PointsWalletEntity> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PointsWalletEntity w where w.userId = :userId")
    Optional<PointsWalletEntity> findByUserIdForUpdate(@Param("userId") Long userId);
}