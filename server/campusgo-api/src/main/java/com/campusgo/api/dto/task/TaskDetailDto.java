package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "任务详情，对齐 Android TaskDetail")
public class TaskDetailDto {

    String id;
    String title;
    String categoryLabel;
    TaskMode mode;
    TaskStatus status;
    String pickupAddress;
    String deliveryAddress;
    String timeLabel;
    String description;
    int rewardCent;
    String rewardYuan;
    String orderNo;
    RunnerSummaryDto runner;

    @JsonProperty("reserveSlotHeld")
    Boolean reserveSlotHeld;

    Integer reserveHoldCount;
}
