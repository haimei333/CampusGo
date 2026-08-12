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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenPort {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Value("${campusgo.jwt.secret:campusgo-dev-secret-change-me-in-production-32chars}")
    private String jwtSecret;

    @Value("${campusgo.jwt.access-expire-days:7}")
    private long accessExpireDays;

    @Value("${campusgo.jwt.refresh-expire-days:30}")
    private long refreshExpireDays;

    @Override
    public AuthTokens issueTokens(UserProfile profile) {
        String accessToken = buildToken(profile.getId(), TYPE_ACCESS, Duration.ofDays(accessExpireDays));
        String refreshToken = buildToken(profile.getId(), TYPE_REFRESH, Duration.ofDays(refreshExpireDays));
        storeRefreshToken(profile.getId(), refreshToken);
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(accessExpireDays * 24 * 3600)
                .userProfile(profile)
                .build();
    }

    @Override
    public AuthTokens refresh(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE))) {
            throw BusinessException.of(ErrorCodes.UNAUTHORIZED, "无效的 Refresh Token");
        }
        long userId = Long.parseLong(claims.get(CLAIM_USER_ID).toString());
        if (!isRefreshTokenValid(userId, refreshToken)) {
            throw BusinessException.of(ErrorCodes.UNAUTHORIZED, "Refresh Token 已失效");
        }
        UserProfile profile = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "用户不存在"));
        revoke(userId, refreshToken);
        return issueTokens(profile);
    }

    @Override
    public void revoke(long userId, String refreshToken) {
        redisTemplate.delete(refreshKey(userId, refreshToken));
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
                .signWith(signingKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw BusinessException.of(ErrorCodes.TOKEN_EXPIRED, "Token 无效或已过期");
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("campusgo.jwt.secret 长度至少 32 字符");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void storeRefreshToken(long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                refreshKey(userId, refreshToken),
                "1",
                Duration.ofDays(refreshExpireDays));
    }

    private boolean isRefreshTokenValid(long userId, String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey(userId, refreshToken)));
    }

    private static String refreshKey(long userId, String refreshToken) {
        return "token:refresh:" + userId + ":" + refreshToken.hashCode();
    }
}
