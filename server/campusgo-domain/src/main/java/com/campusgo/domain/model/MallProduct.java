package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class MallProduct {
    Long id;
    String name;
    String subtitle;
    String category;
    int pointsCost;
    int stock;
    String emoji;
    boolean flashSale;
    boolean enabled;
    Instant createdAt;
}