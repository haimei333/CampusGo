package com.campusgo.api.mock.task;

import com.campusgo.api.dto.task.GroupMemberDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class GroupMemberEntry {

    String id;
    Long userId;
    String name;
    GroupMemberDto.Role role;
    String addressSummary;
    double paidAmount;

    static GroupMemberEntry emptySlot() {
        return new GroupMemberEntry("", null, "", GroupMemberDto.Role.EMPTY_SLOT, "", 0);
    }
}
