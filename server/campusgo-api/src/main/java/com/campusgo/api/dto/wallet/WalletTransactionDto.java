package com.campusgo.api.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "钱包流水")
public class WalletTransactionDto {

    String id;
    String title;
    String timeLabel;
    double amount;
    Type type;

    public enum Type {
        INCOME, EXPENSE
    }
}
