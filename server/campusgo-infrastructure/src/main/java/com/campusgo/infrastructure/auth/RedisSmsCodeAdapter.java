package com.campusgo.infrastructure.auth;

import com.campusgo.application.auth.SmsCodePort;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisSmsCodeAdapter implements SmsCodePort {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_LIMIT = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    @Value("${campusgo.auth.dev-bypass:true}")
    private boolean devBypass;

    @Value("${campusgo.auth.dev-code:123456}")
    private String devCode;

    @Override
    public void send(String phone, String scene) {
        String limitKey = "sms:limit:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw BusinessException.of(ErrorCodes.TOO_MANY_REQUESTS, "发送过于频繁，请稍后再试");
        }

        String code = devBypass ? devCode : randomCode();
        String codeKey = codeKey(phone, scene);
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL);
        redisTemplate.opsForValue().set(limitKey, "1", SEND_LIMIT);

        if (devBypass) {
            log.info("[DEV] SMS code for {} scene {} => {}", phone, scene, code);
        } else {
            log.info("SMS sent to {} scene {}", phone, scene);
        }
    }

    @Override
    public boolean verify(String phone, String scene, String code) {
        if (devBypass && devCode.equals(code)) {
            return true;
        }
        String stored = redisTemplate.opsForValue().get(codeKey(phone, scene));
        if (stored != null && stored.equals(code)) {
            redisTemplate.delete(codeKey(phone, scene));
            return true;
        }
        return false;
    }

    private static String codeKey(String phone, String scene) {
        return "sms:code:" + phone + ":" + scene;
    }

    private static String randomCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
