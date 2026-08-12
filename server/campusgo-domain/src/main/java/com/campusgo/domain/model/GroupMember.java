package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GroupMember {
    Long id;
    Long userId;
    String name;
    String role;
    String addressSummary;
    int shareCent;
    String payStatus;
}
