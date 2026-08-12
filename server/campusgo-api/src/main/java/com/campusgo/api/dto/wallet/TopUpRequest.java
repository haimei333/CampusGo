package com.campusgo.api.dto.wallet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopUpRequest {

    /** 充值金额（元） */
    @NotNull(message = "请输入充值金额")
    @Min(value = 1, message = "最低充值 1 元")
    @Max(value = 5000, message = "单次最高充值 5000 元")
    private Double amount;
}
