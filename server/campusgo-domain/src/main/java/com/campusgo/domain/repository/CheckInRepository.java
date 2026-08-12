package com.campusgo.domain.repository;

import com.campusgo.domain.model.CheckInRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository {

    Optional<CheckInRecord> findByUserAndDate(long userId, LocalDate date);

    Optional<CheckInRecord> findLatestBefore(long userId, LocalDate date);

    CheckInRecord save(CheckInRecord record);

    List<CheckInRecord> findMonthRecords(long userId, int year, int month);
}