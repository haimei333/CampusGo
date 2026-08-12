package com.campusgo.domain.repository;

import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.UserProfile;

import java.util.Optional;

public interface UserRepository {

    Optional<UserProfile> findById(long userId);

    Optional<UserProfile> findByPhone(String phone);

    UserProfile createUser(String phone, String nickname, String rawPassword);

    boolean matchesPassword(String phone, String rawPassword);

    void setPassword(long userId, String rawPassword);

    boolean hasPassword(String phone);

    void updateNickname(long userId, String nickname);

    void updateActiveRole(long userId, UserRole role);

    void updateCampusStatus(long userId, CampusVerifyStatus status);

    CampusVerifyStatus getCampusStatus(long userId);

    /** 调整信用分，结果 clamp 在 0–1000 */
    int adjustCreditScore(long userId, int delta);
}
