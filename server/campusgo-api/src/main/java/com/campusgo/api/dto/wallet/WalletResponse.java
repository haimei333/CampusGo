package com.campusgo.api.dto.wallet;

import com.campusgo.domain.model.WalletSummary;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WalletResponse {

    long balanceCent;
    long frozenCent;
    long totalIncomeCent;
    String balanceYuan;

    public static WalletResponse from(WalletSummary summary) {
        return WalletResponse.builder()
                .balanceCent(summary.getBalanceCent())
                .frozenCent(summary.getFrozenCent())
                .totalIncomeCent(summary.getTotalIncomeCent())
                .balanceYuan(summary.getBalanceYuan())
                .build();
    }
}
