package com.campusgo.domain.model;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class Task {
    Long id;
    String taskNo;
    long publisherId;
    Long runnerId;
    TaskMode mode;
    TaskCategory category;
    String title;
    String description;
    TaskStatus status;
    String pickupName;
    String dropoffName;
    String timeLabel;
    int rewardCent;
    int baseRewardCent;
    int escrowCent;
    Integer groupTargetCount;
    Integer groupJoinedCount;
    String deliveryPhotoUrl;
    String cancelReason;
    String runnerName;
    Float runnerRating;
    Integer runnerCredit;
    Integer runnerCompletedOrders;
    Instant createdAt;
    Instant updatedAt;
    Instant acceptedAt;
    Instant completedAt;
}
