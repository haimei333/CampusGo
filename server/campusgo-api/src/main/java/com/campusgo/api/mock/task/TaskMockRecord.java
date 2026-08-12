package com.campusgo.api.mock.task;

import com.campusgo.api.dto.task.TaskListItemDto;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import lombok.Builder;
import lombok.Data;

/**
 * 内存任务记录，Mock 阶段使用。
 */
@Data
@Builder
public class TaskMockRecord {

    String id;
    TaskListScope listScope;
    UserRole reserveForRole;

    long publisherId;
    Long runnerId;

    String title;
    String statusLabel;
    String description;
    int rewardCent;
    TaskCategory category;
    TaskMode mode;
    TaskStatus status;
    TaskListItemDto.NavTarget navTarget;

    String pickupAddress;
    String deliveryAddress;
    String timeLabel;
    String orderNo;

    String runnerName;
    float runnerRating;
    int runnerCredit;
    int runnerCompletedOrders;

    public enum TaskListScope {
        HALL,
        MINE_PUBLISH,
        MINE_ACCEPTED,
        POOL,
        RESERVE
    }
}
