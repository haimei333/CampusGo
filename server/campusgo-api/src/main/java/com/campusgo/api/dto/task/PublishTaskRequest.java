package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class PublishTaskRequest {

    private String draftId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TaskMode mode;

    @NotNull
    private TaskCategory category;

    @NotBlank
    private String pickupAddress;

    @NotBlank
    private String deliveryAddress;

    private String timeLabel;

    @Min(1)
    private int rewardCent;

    private Integer groupTargetCount;
    private Instant reserveAt;
}
