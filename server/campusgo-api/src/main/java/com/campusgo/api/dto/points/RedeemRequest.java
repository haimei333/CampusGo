package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "兑换请求")
public class RedeemRequest {

    @Positive
    @Schema(description = "商品ID")
    private long productId;

    @Schema(description = "收货地址（实物商品必填）")
    private String address;
}