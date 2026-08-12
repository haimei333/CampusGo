package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.WalletLedgerEntry;
import com.campusgo.domain.repository.WalletLedgerRepository;
import com.campusgo.infrastructure.persistence.entity.WalletLedgerEntity;
import com.campusgo.infrastructure.persistence.jpa.WalletLedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class WalletLedgerRepositoryImpl implements WalletLedgerRepository {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final WalletLedgerJpaRepository jpaRepository;

    @Override
    public WalletLedgerEntry append(long userId, String type, int amountCent, String direction,
                                    int balanceAfterCent, Long taskId, String remark) {
        WalletLedgerEntity entity = new WalletLedgerEntity();
        entity.setLedgerNo("L" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + SEQ.incrementAndGet());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setAmountCent(Math.abs(amountCent));
        entity.setDirection(direction);
        entity.setBalanceAfterCent(balanceAfterCent);
        entity.setTaskId(taskId);
        entity.setRemark(remark);
        entity.setCreatedAt(Instant.now());
        return toModel(jpaRepository.save(entity));
    }

    @Override
    public List<WalletLedgerEntry> listByUser(long userId, int limit) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(Math.max(limit, 1))
                .map(this::toModel)
                .toList();
    }

    private WalletLedgerEntry toModel(WalletLedgerEntity e) {
        return WalletLedgerEntry.builder()
                .id(e.getId())
                .ledgerNo(e.getLedgerNo())
                .userId(e.getUserId())
                .type(e.getType())
                .amountCent(e.getAmountCent())
                .direction(e.getDirection())
                .balanceAfterCent(e.getBalanceAfterCent())
                .taskId(e.getTaskId())
                .remark(e.getRemark())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
