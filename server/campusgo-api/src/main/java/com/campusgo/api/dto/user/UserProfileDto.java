package com.campusgo.api.dto.user;

import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.UserProfile;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileDto {

    Long id;
    String phone;
    String nickname;
    String avatarUrl;
    int creditScore;
    UserRole activeRole;
    CampusVerifyStatus campusStatus;

    public static UserProfileDto from(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .phone(maskPhone(profile.getPhone()))
                .nickname(profile.getNickname())
                .avatarUrl(profile.getAvatarUrl())
                .creditScore(profile.getCreditScore())
                .activeRole(profile.getActiveRole())
                .campusStatus(profile.getCampusStatus())
                .build();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
