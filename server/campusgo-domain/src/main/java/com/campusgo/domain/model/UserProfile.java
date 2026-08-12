package com.campusgo.domain.model;

import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfile {
    Long id;
    String phone;
    String nickname;
    String avatarUrl;
    int creditScore;
    UserRole activeRole;
    CampusVerifyStatus campusStatus;
}
