package com.campusgo.api.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "跑腿员摘要")
public class RunnerSummaryDto {
    String name;
    float rating;
    int creditScore;
    int completedOrders;
}
