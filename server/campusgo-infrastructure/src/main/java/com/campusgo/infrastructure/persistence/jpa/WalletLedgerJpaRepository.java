package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.WalletLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLedgerJpaRepository extends JpaRepository<WalletLedgerEntity, Long> {

    List<WalletLedgerEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
