package com.campusgo.api.controller;

import com.campusgo.api.dto.ai.ChatRequestDto;
import com.campusgo.api.dto.ai.ChatResponseDto;
import com.campusgo.api.dto.ai.HistoryResponseDto;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.ai.AiService;
import com.campusgo.domain.model.AiConversation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody ChatRequestDto request) {

        Long userId = authUser.userId();
        String reply = aiService.chat(userId, request.getSessionId(), request.getMessage());

        return ResponseEntity.ok(new ChatResponseDto(request.getSessionId(), reply));
    }

    @GetMapping("/history")
    public ResponseEntity<HistoryResponseDto> getHistory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String sessionId) {

        Long userId = authUser.userId();

        if (sessionId == null || sessionId.isEmpty()) {
            List<String> sessions = aiService.getSessionIds(userId);
            return ResponseEntity.ok(new HistoryResponseDto(sessions, null));
        }

        List<AiConversation> history = aiService.getHistory(userId, sessionId);
        List<HistoryResponseDto.MessageDto> messages = history.stream()
                .map(conv -> new HistoryResponseDto.MessageDto(
                        conv.getRole(),
                        conv.getContent(),
                        conv.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new HistoryResponseDto(null, messages));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> clearSession(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String sessionId) {

        Long userId = authUser.userId();
        aiService.clearSession(userId, sessionId);

        return ResponseEntity.ok().build();
    }
}