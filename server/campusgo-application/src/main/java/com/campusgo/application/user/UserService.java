package com.campusgo.application.user;

import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.UserProfile;

public interface UserService {

    UserProfile getCurrentUser(long userId);

    UserProfile updateNickname(long userId, String nickname);

    UserProfile switchRole(long userId, UserRole role);

    /** 提交校园卡认证（演示环境自动通过） */
    UserProfile submitCampusAuth(long userId, String realName, String studentId);
}
