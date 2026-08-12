package com.campusgo.api.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "拼单成员")
public class GroupMemberDto {

    String id;
    String name;
    Role role;
    String addressSummary;
    double paidAmount;
    boolean joined;

    public enum Role {
        CREATOR, MEMBER, EMPTY_SLOT
    }
}
