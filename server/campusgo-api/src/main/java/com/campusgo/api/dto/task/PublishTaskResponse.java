package com.campusgo.api.dto.task;

import com.campusgo.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PublishTaskResponse {
    String taskId;
    String taskNo;
    TaskStatus status;
}
