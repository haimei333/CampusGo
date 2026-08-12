package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "积分流水")
public class PointsTransactionDto {

    String id;
    String type;       // IN / OUT
    int amount;
    String bizType;
    String remark;
    String timeLabel;
}