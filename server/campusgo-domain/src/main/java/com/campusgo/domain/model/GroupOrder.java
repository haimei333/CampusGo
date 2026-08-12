package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class GroupOrder {
    Task task;
    double totalReward;
    double sharePerPerson;
    int maxMembers;
    int joinedCount;
    boolean viewerJoined;
    boolean viewerIsCreator;
    boolean full;
    List<GroupMember> members;
}
