package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokens {
    String accessToken;
    String refreshToken;
    long expiresInSeconds;
    UserProfile userProfile;
}
