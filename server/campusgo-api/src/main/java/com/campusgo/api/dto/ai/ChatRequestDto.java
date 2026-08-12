package com.campusgo.api.dto.ai;

import lombok.Data;

@Data
public class ChatRequestDto {
    private String sessionId;
    private String message;
}
