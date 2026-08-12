package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class PointsTransaction {
    Long id;
    long userId;
    String type;
    int amount;
    int balanceAfter;
    String bizType;
    String bizId;
    String remark;
    Instant createdAt;
}