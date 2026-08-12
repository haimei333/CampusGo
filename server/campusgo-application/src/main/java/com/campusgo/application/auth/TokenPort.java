package com.campusgo.application.auth;

import com.campusgo.domain.model.AuthTokens;
import com.campusgo.domain.model.UserProfile;

public interface TokenPort {

    AuthTokens issueTokens(UserProfile profile);

    AuthTokens refresh(String refreshToken);

    void revoke(long userId, String refreshToken);

    Long parseUserId(String accessToken);
}
