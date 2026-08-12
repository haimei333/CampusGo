package com.campusgo.infrastructure.persistence;

import com.campusgo.application.auth.PasswordHasher;
import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.infrastructure.persistence.entity.AppUserEntity;
import com.campusgo.infrastructure.persistence.jpa.AppUserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final AppUserJpaRepository jpaRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public Optional<UserProfile> findById(long userId) {
        return jpaRepository.findById(userId).map(this::toProfile);
    }

    @Override
    public Optional<UserProfile> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(this::toProfile);
    }

    @Override
    public UserProfile createUser(String phone, String nickname, String rawPassword) {
        AppUserEntity entity = new AppUserEntity();
        entity.setPhone(phone);
        entity.setNickname(nickname);
        if (StringUtils.hasText(rawPassword)) {
            entity.setPasswordHash(passwordHasher.hash(rawPassword));
        }
        return toProfile(jpaRepository.save(entity));
    }

    @Override
    public boolean matchesPassword(String phone, String rawPassword) {
        return jpaRepository.findByPhone(phone)
                .map(entity -> passwordHasher.matches(rawPassword, entity.getPasswordHash()))
                .orElse(false);
    }

    @Override
    public void setPassword(long userId, String rawPassword) {
        AppUserEntity entity = jpaRepository.findById(userId).orElseThrow();
        entity.setPasswordHash(passwordHasher.hash(rawPassword));
        jpaRepository.save(entity);
    }

    @Override
    public boolean hasPassword(String phone) {
        return jpaRepository.findByPhone(phone)
                .map(entity -> StringUtils.hasText(entity.getPasswordHash()))
                .orElse(false);
    }

    @Override
    public void updateNickname(long userId, String nickname) {
        AppUserEntity entity = jpaRepository.findById(userId)
                .orElseThrow();
        entity.setNickname(nickname);
        jpaRepository.save(entity);
    }

    @Override
    public void updateActiveRole(long userId, UserRole role) {
        AppUserEntity entity = jpaRepository.findById(userId)
                .orElseThrow();
        entity.setActiveRole(role);
        jpaRepository.save(entity);
    }

    @Override
    public void updateCampusStatus(long userId, CampusVerifyStatus status) {
        AppUserEntity entity = jpaRepository.findById(userId)
                .orElseThrow();
        entity.setCampusStatus(status);
        jpaRepository.save(entity);
    }

    @Override
    public CampusVerifyStatus getCampusStatus(long userId) {
        return jpaRepository.findById(userId)
                .map(AppUserEntity::getCampusStatus)
                .orElse(CampusVerifyStatus.NONE);
    }

    @Override
    public int adjustCreditScore(long userId, int delta) {
        AppUserEntity entity = jpaRepository.findById(userId).orElseThrow();
        int next = Math.max(0, Math.min(1000, entity.getCreditScore() + delta));
        entity.setCreditScore(next);
        jpaRepository.save(entity);
        return next;
    }

    private UserProfile toProfile(AppUserEntity entity) {
        return UserProfile.builder()
                .id(entity.getId())
                .phone(entity.getPhone())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .creditScore(entity.getCreditScore())
                .activeRole(entity.getActiveRole())
                .campusStatus(entity.getCampusStatus())
                .build();
    }
}
