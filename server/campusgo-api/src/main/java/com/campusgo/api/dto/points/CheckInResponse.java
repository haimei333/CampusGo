package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "签到结果")
public class CheckInResponse {

    int rewardPoints;
    int newStreak;
    int newBalance;
}