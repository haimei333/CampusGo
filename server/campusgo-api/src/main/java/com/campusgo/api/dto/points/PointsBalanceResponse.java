package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "积分余额")
public class PointsBalanceResponse {

    int balance;
    int totalEarned;
}