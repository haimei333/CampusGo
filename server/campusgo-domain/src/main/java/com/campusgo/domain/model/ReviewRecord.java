package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ReviewRecord {
    long id;
    long taskId;
    long fromUserId;
    long toUserId;
    int score;
    List<String> tags;
    String content;
    boolean isDefault;
    Instant createdAt;
}
