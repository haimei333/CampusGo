package com.campusgo.api.config;

import com.campusgo.application.auth.AuthServiceImpl;
import com.campusgo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 为尚无 password_hash 的演示账号补齐默认密码 123456。
 */
@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class DemoPasswordBackfill implements ApplicationRunner {

    private static final String[] DEMO_PHONES = {
            "13800138000",
            "13900139000"
    };

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String phone : DEMO_PHONES) {
            userRepository.findByPhone(phone).ifPresent(profile -> {
                if (!userRepository.hasPassword(phone)) {
                    userRepository.setPassword(profile.getId(), AuthServiceImpl.DEMO_DEFAULT_PASSWORD);
                    log.info("Backfilled demo password for {}", phone);
                }
            });
        }
    }
}
