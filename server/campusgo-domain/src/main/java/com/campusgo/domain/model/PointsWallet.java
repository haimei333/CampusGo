package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class PointsWallet {
    Long id;
    long userId;
    int balance;
    int totalEarned;
    Instant createdAt;
    Instant updatedAt;
}