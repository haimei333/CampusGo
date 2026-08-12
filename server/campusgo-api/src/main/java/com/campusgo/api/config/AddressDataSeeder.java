package com.campusgo.api.config;

import com.campusgo.application.address.AddressService;
import com.campusgo.domain.enums.AddressTag;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.AddressRepository;
import com.campusgo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 为演示账号补齐常用地址（仅当该用户尚无地址时）。
 */
@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class AddressDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AddressService addressService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedForPhone("13800138000");
    }

    private void seedForPhone(String phone) {
        UserProfile user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            return;
        }
        if (addressRepository.countByUserId(user.getId()) > 0) {
            return;
        }
        addressService.create(user.getId(), "38号楼 512室", "海淀区颐和园路5号北京大学", AddressTag.DORM, true);
        addressService.create(user.getId(), "理科教学楼 301教室", "海淀区颐和园路5号北京大学", AddressTag.BUILDING, false);
        addressService.create(user.getId(), "图书馆 2层自习室", "海淀区颐和园路5号北京大学", AddressTag.LIBRARY, false);
        addressService.create(user.getId(), "农园食堂 1层", "海淀区颐和园路5号北京大学", AddressTag.CANTEEN, false);
        log.info("Seeded demo addresses for {}", phone);
    }
}
