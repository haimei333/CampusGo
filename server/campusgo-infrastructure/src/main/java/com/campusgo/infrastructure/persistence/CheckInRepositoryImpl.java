package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.CheckInRecord;
import com.campusgo.domain.repository.CheckInRepository;
import com.campusgo.infrastructure.persistence.entity.CheckInRecordEntity;
import com.campusgo.infrastructure.persistence.jpa.CheckInJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CheckInRepositoryImpl implements CheckInRepository {

    private final CheckInJpaRepository jpaRepository;

    @Override
    public Optional<CheckInRecord> findByUserAndDate(long userId, LocalDate date) {
        return jpaRepository.findByUserIdAndCheckInDate(userId, date)
                .map(this::toModel);
    }

    @Override
    public Optional<CheckInRecord> findLatestBefore(long userId, LocalDate date) {
        return jpaRepository.findFirstByUserIdOrderByCheckInDateDesc(userId)
                .filter(r -> !r.getCheckInDate().isBefore(date.minusDays(1)))
                .map(this::toModel);
    }

    @Override
    public CheckInRecord save(CheckInRecord record) {
        CheckInRecordEntity entity = toEntity(record);
        CheckInRecordEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    @Override
    public List<CheckInRecord> findMonthRecords(long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return jpaRepository.findByUserIdAndCheckInDateBetween(userId, start, end)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    private CheckInRecord toModel(CheckInRecordEntity entity) {
        return CheckInRecord.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .checkInDate(entity.getCheckInDate())
                .streak(entity.getStreak())
                .rewardPoints(entity.getRewardPoints())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private CheckInRecordEntity toEntity(CheckInRecord model) {
        CheckInRecordEntity entity = new CheckInRecordEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setCheckInDate(model.getCheckInDate());
        entity.setStreak(model.getStreak());
        entity.setRewardPoints(model.getRewardPoints());
        entity.setCreatedAt(model.getCreatedAt());
        return entity;
    }
}