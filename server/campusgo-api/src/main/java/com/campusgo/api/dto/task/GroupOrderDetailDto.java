package com.campusgo.api.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "拼单详情")
public class GroupOrderDetailDto {

    String taskId;
    String title;
    String categoryLabel;
    String pickupAddress;
    String deliverySummary;
    double totalReward;
    double sharePerPerson;
    int maxMembers;
    int joinedCount;
    String timeLabel;
    boolean viewerJoined;
    boolean viewerIsCreator;
    boolean full;
    List<GroupMemberDto> members;
}
