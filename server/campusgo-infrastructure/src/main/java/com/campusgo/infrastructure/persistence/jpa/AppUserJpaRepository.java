package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, Long> {

    Optional<AppUserEntity> findByPhone(String phone);
}
