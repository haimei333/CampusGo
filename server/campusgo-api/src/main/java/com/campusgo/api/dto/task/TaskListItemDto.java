package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "任务列表项，对齐 Android TaskListItem")
public class TaskListItemDto {

    String id;
    String title;
    String statusLabel;
    String description;
    int priceCent;
    String priceYuan;
    TaskCategory category;
    TaskMode mode;
    TaskStatus status;
    NavTarget navTarget;
    UserRole reserveForRole;

    public enum NavTarget {
        T01, T06, T07
    }
}
