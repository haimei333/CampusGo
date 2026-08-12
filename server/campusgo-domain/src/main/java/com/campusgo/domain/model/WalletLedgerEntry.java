package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class WalletLedgerEntry {
    Long id;
    String ledgerNo;
    long userId;
    String type;
    int amountCent;
    String direction;
    int balanceAfterCent;
    Long taskId;
    String remark;
    Instant createdAt;
}
