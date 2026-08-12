package com.campusgo.application.user;

import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int MIN_RUNNER_CREDIT = 400;

    private final UserRepository userRepository;

    @Override
    public UserProfile getCurrentUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "用户不存在"));
    }

    @Override
    @Transactional
    public UserProfile updateNickname(long userId, String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() > 32) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "昵称长度应为 1–32 字");
        }
        userRepository.updateNickname(userId, nickname.trim());
        return getCurrentUser(userId);
    }

    @Override
    @Transactional
    public UserProfile switchRole(long userId, UserRole role) {
        UserProfile current = getCurrentUser(userId);
        if (role == UserRole.RUNNER) {
            if (userRepository.getCampusStatus(userId) != CampusVerifyStatus.APPROVED) {
                throw BusinessException.of(ErrorCodes.FORBIDDEN, "请先完成校园卡认证");
            }
            if (current.getCreditScore() < MIN_RUNNER_CREDIT) {
                throw BusinessException.of(ErrorCodes.FORBIDDEN, "信用分不足，暂无法切换为跑腿员");
            }
        }
        userRepository.updateActiveRole(userId, role);
        return getCurrentUser(userId);
    }

    @Override
    @Transactional
    public UserProfile submitCampusAuth(long userId, String realName, String studentId) {
        if (realName == null || realName.isBlank()) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "请填写姓名");
        }
        if (studentId == null || studentId.isBlank()) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "请填写学号");
        }
        // 演示环境：提交即通过
        userRepository.updateCampusStatus(userId, CampusVerifyStatus.APPROVED);
        return getCurrentUser(userId);
    }
}
