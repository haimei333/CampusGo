package com.campusgo.infrastructure.auth;

import com.campusgo.application.auth.TokenPort;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.AuthTokens;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("test")
public class TestAuthConfig {

    @Bean
    TokenPort inMemoryTokenPort(
            @Value("${campusgo.jwt.secret:campusgo-dev-secret-change-me-in-production-32chars}") String secret,
            UserRepository userRepository) {
        return new InMemoryTokenPort(secret, userRepository);
    }

    static class InMemoryTokenPort implements TokenPort {

        private static final String CLAIM_USER_ID = "uid";
        private static final String CLAIM_TOKEN_TYPE = "type";
        private static final String TYPE_ACCESS = "access";
        private static final String TYPE_REFRESH = "refresh";

        private final SecretKey signingKey;
        private final UserRepository userRepository;
        private final Map<String, Long> refreshTokens = new ConcurrentHashMap<>();

        InMemoryTokenPort(String secret, UserRepository userRepository) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            this.userRepository = userRepository;
        }

        @Override
        public AuthTokens issueTokens(UserProfile profile) {
            String accessToken = buildToken(profile.getId(), TYPE_ACCESS, Duration.ofDays(7));
            String refreshToken = buildToken(profile.getId(), TYPE_REFRESH, Duration.ofDays(30));
            refreshTokens.put(refreshToken, profile.getId());
            return AuthTokens.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresInSeconds(7 * 24 * 3600)
                    .userProfile(profile)
                    .build();
        }

        @Override
        public AuthTokens refresh(String refreshToken) {
            Long userId = refreshTokens.remove(refreshToken);
            if (userId == null) {
                throw BusinessException.of(ErrorCodes.UNAUTHORIZED, "Refresh Token 已失效");
            }
            UserProfile profile = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "用户不存在"));
            return issueTokens(profile);
        }

        @Override
        public void revoke(long userId, String refreshToken) {
            refreshTokens.remove(refreshToken);
        }

        @Override
        public Long parseUserId(String accessToken) {
            Claims claims = parseClaims(accessToken);
            if (!TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE))) {
                throw BusinessException.of(ErrorCodes.UNAUTHORIZED, "无效的 Access Token");
            }
            return Long.parseLong(claims.get(CLAIM_USER_ID).toString());
        }

        private String buildToken(long userId, String type, Duration ttl) {
            Instant now = Instant.now();
            return Jwts.builder()
                    .id(UUID.randomUUID().toString())
                    .subject(String.valueOf(userId))
                    .claim(CLAIM_USER_ID, userId)
                    .claim(CLAIM_TOKEN_TYPE, type)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(ttl)))
                    .signWith(signingKey)
                    .compact();
        }

        private Claims parseClaims(String token) {
            try {
                return Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception ex) {
                throw BusinessException.of(ErrorCodes.TOKEN_EXPIRED, "Token 无效或已过期");
            }
        }
    }
}
