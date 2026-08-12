package com.campusgo.api.dto.auth;

import com.campusgo.api.dto.user.UserProfileDto;
import com.campusgo.domain.model.AuthTokens;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {

    String accessToken;
    String refreshToken;
    long expiresIn;
    UserProfileDto userProfile;

    public static LoginResponse from(AuthTokens tokens) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .expiresIn(tokens.getExpiresInSeconds())
                .userProfile(UserProfileDto.from(tokens.getUserProfile()))
                .build();
    }
}
