package com.campusgo.api.dto.chat;

import com.campusgo.domain.enums.ChatMsgType;
import com.campusgo.domain.model.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatMessageDto {

    private String id;
    private String conversationId;
    private String taskId;
    private String senderId;
    private ChatMsgType msgType;
    private String content;
    private boolean mine;
    private boolean read;
    private Instant createdAt;

    public static ChatMessageDto from(ChatMessage message, long viewerId) {
        boolean mine = message.getSenderId() != null && message.getSenderId() == viewerId;
        return ChatMessageDto.builder()
                .id(String.valueOf(message.getId()))
                .conversationId(String.valueOf(message.getConversationId()))
                .taskId(String.valueOf(message.getTaskId()))
                .senderId(message.getSenderId() == null ? null : String.valueOf(message.getSenderId()))
                .msgType(message.getMsgType())
                .content(message.getContent())
                .mine(mine)
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
