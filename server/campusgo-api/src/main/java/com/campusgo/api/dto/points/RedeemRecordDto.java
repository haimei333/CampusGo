package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "兑换记录")
public class RedeemRecordDto {

    String id;
    String productName;
    int pointsCost;
    String address;
    String status;
    String timeLabel;
}