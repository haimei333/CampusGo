package com.campusgo.api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class HistoryResponseDto {
    private List<String> sessions;
    private List<MessageDto> messages;

    @Data
    @AllArgsConstructor
    public static class MessageDto {
        private String role;
        private String content;
        private Instant timestamp;
    }
}
