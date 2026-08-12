package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class ChatConversation {
    long id;
    long taskId;
    long publisherId;
    long runnerId;
    String lastMsgPreview;
    Instant lastMsgAt;
    boolean archived;
    Instant createdAt;
    Instant updatedAt;
}
