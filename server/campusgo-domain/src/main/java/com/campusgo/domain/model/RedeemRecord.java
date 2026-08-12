package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class RedeemRecord {
    Long id;
    long userId;
    long productId;
    String productName;
    int pointsCost;
    String address;
    String status;
    Instant createdAt;
}