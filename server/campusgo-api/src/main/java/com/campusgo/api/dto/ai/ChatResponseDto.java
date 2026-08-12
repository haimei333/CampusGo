package com.campusgo.api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponseDto {
    private String sessionId;
    private String reply;
}
