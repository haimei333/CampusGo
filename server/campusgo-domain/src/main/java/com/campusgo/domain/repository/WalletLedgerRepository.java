package com.campusgo.domain.repository;

import com.campusgo.domain.model.WalletLedgerEntry;

import java.util.List;

public interface WalletLedgerRepository {

    WalletLedgerEntry append(long userId, String type, int amountCent, String direction,
                             int balanceAfterCent, Long taskId, String remark);

    List<WalletLedgerEntry> listByUser(long userId, int limit);
}
