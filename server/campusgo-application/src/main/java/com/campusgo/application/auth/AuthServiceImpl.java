package com.campusgo.application.auth;

import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.AuthTokens;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    public static final String DEMO_DEFAULT_PASSWORD = "123456";

    private final TokenPort tokenPort;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public AuthTokens register(String phone, String password) {
        validatePhone(phone);
        validatePassword(password);

        if (userRepository.findByPhone(phone).isPresent()) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "该手机号已注册，请直接登录");
        }

        UserProfile created = userRepository.createUser(phone, defaultNickname(phone), password);
        walletRepository.initWallet(created.getId());
        return tokenPort.issueTokens(created);
    }

    @Override
    @Transactional
    public AuthTokens login(String phone, String password) {
        validatePhone(phone);
        validatePassword(password);

        UserProfile profile = userRepository.findByPhone(phone)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "账号不存在，请先注册"));

        if (!userRepository.hasPassword(phone)) {
            if (!DEMO_DEFAULT_PASSWORD.equals(password)) {
                throw BusinessException.of(ErrorCodes.UNAUTHORIZED,
                        "该账号尚未设置密码，演示环境请使用默认密码 123456");
            }
            userRepository.setPassword(profile.getId(), password);
        } else if (!userRepository.matchesPassword(phone, password)) {
            throw BusinessException.of(ErrorCodes.UNAUTHORIZED, "手机号或密码错误");
        }

        return tokenPort.issueTokens(profile);
    }

    @Override
    public AuthTokens refresh(String refreshToken) {
        return tokenPort.refresh(refreshToken);
    }

    @Override
    public void logout(long userId, String refreshToken) {
        tokenPort.revoke(userId, refreshToken);
    }

    private static void validatePhone(String phone) {
        if (!StringUtils.hasText(phone) || !phone.matches("^1\\d{10}$")) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "手机号格式不正确");
        }
    }

    private static void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "请输入密码");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "密码长度应为 6–32 位");
        }
    }

    private static String defaultNickname(String phone) {
        return "用户" + phone.substring(phone.length() - 4);
    }
}
