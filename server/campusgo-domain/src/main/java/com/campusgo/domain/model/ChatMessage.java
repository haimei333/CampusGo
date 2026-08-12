package com.campusgo.domain.model;

import com.campusgo.domain.enums.ChatMsgType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class ChatMessage {
    long id;
    long conversationId;
    long taskId;
    Long senderId;
    ChatMsgType msgType;
    String content;
    boolean read;
    Instant createdAt;
}
