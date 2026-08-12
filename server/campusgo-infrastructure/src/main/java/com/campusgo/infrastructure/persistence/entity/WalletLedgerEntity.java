package com.campusgo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "wallet_ledger")
public class WalletLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_no", nullable = false, unique = true, length = 32)
    private String ledgerNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "amount_cent", nullable = false)
    private int amountCent;

    @Column(name = "direction", nullable = false, length = 8)
    private String direction;

    @Column(name = "balance_after_cent", nullable = false)
    private int balanceAfterCent;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "remark", length = 256)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
