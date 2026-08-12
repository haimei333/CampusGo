package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.MallProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MallProductJpaRepository extends JpaRepository<MallProductEntity, Long> {

    List<MallProductEntity> findByEnabledTrue();
}