package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveDraftRequest {

    private String title;
    private String description;

    @NotNull
    private TaskMode mode;

    @NotNull
    private TaskCategory category;

    private String pickupAddress;
    private String deliveryAddress;
    private String timeLabel;

    @Min(0)
    private int rewardCent;
}
