package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@Schema(description = "发布草稿")
public class TaskDraftDto {

    String id;
    String title;
    String description;
    TaskMode mode;
    TaskCategory category;
    String pickupAddress;
    String deliveryAddress;
    String timeLabel;
    int rewardCent;
    String rewardYuan;
    Instant updatedAt;
}
