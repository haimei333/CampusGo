package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.chat.ChatMessageDto;
import com.campusgo.api.dto.chat.ConversationDto;
import com.campusgo.api.dto.chat.SendMessageRequest;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.chat.ChatService;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.ChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Chat", description = "文字聊天")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatService chatService;

    @Operation(summary = "会话列表")
    @GetMapping
    public ApiResponse<List<ConversationDto>> list(@AuthenticationPrincipal AuthUser user) {
        List<ConversationDto> list = chatService.listConversations(user.userId()).stream()
                .map(ConversationDto::from)
                .toList();
        return ApiResponse.ok(list);
    }

    @Operation(summary = "会话详情")
    @GetMapping("/{id}")
    public ApiResponse<ConversationDto> get(@AuthenticationPrincipal AuthUser user,
                                            @PathVariable("id") long id) {
        return ApiResponse.ok(ConversationDto.from(chatService.getConversation(user.userId(), id)));
    }

    @Operation(summary = "按任务获取或创建会话")
    @GetMapping("/by-task/{taskId}")
    public ApiResponse<ConversationDto> byTask(@AuthenticationPrincipal AuthUser user,
                                               @PathVariable("taskId") long taskId) {
        return ApiResponse.ok(ConversationDto.from(chatService.getOrCreateByTask(user.userId(), taskId)));
    }

    @Operation(summary = "历史消息（升序，beforeId 游标）")
    @GetMapping("/{id}/messages")
    public ApiResponse<List<ChatMessageDto>> messages(@AuthenticationPrincipal AuthUser user,
                                                      @PathVariable("id") long id,
                                                      @RequestParam(value = "beforeId", required = false) Long beforeId,
                                                      @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<ChatMessageDto> list = chatService.listMessages(user.userId(), id, beforeId, limit).stream()
                .map(m -> ChatMessageDto.from(m, user.userId()))
                .toList();
        return ApiResponse.ok(list);
    }

    @Operation(summary = "发送文字消息")
    @PostMapping("/{id}/messages")
    public ApiResponse<ChatMessageDto> send(@AuthenticationPrincipal AuthUser user,
                                            @PathVariable("id") long id,
                                            @Valid @RequestBody SendMessageRequest request) {
        if (request.getMsgType() != null
                && !"TEXT".equalsIgnoreCase(request.getMsgType().trim())) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "一期仅支持文字消息");
        }
        ChatMessage saved = chatService.sendText(user.userId(), id, request.getContent());
        return ApiResponse.ok(ChatMessageDto.from(saved, user.userId()));
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{id}/read")
    public ApiResponse<Void> read(@AuthenticationPrincipal AuthUser user,
                                  @PathVariable("id") long id) {
        chatService.markRead(user.userId(), id);
        return ApiResponse.ok(null);
    }
}
