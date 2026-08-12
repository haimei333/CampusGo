package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class CheckInRecord {
    Long id;
    long userId;
    LocalDate checkInDate;
    int streak;
    int rewardPoints;
    Instant createdAt;
}