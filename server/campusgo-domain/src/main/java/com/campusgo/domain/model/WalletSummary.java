package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WalletSummary {
    long balanceCent;
    long frozenCent;
    long totalIncomeCent;
    String balanceYuan;
}
