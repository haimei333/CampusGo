package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.CheckInRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInJpaRepository extends JpaRepository<CheckInRecordEntity, Long> {

    Optional<CheckInRecordEntity> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    Optional<CheckInRecordEntity> findFirstByUserIdOrderByCheckInDateDesc(Long userId);

    List<CheckInRecordEntity> findByUserIdAndCheckInDateBetween(Long userId, LocalDate start, LocalDate end);
}