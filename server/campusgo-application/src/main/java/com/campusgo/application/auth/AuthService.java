package com.campusgo.application.auth;

import com.campusgo.domain.model.AuthTokens;

public interface AuthService {

    /** 注册新账号；手机号已存在则失败 */
    AuthTokens register(String phone, String password);

    /** 仅已注册账号可登录；不存在则失败 */
    AuthTokens login(String phone, String password);

    AuthTokens refresh(String refreshToken);

    void logout(long userId, String refreshToken);
}
