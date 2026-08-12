package com.campusgo.api.dto.chat;

import com.campusgo.application.chat.ChatService;
import com.campusgo.domain.model.ChatConversation;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConversationDto {

    private String id;
    private String taskId;
    private String peerName;
    private String peerRole;
    private String preview;
    private Instant lastMsgAt;
    private int unreadCount;
    private String taskTitle;
    private double taskReward;
    private boolean archived;

    public static ConversationDto from(ChatService.ConversationView view) {
        ChatConversation c = view.conversation();
        return ConversationDto.builder()
                .id(String.valueOf(c.getId()))
                .taskId(String.valueOf(c.getTaskId()))
                .peerName(view.peerNickname())
                .peerRole(view.peerRole())
                .preview(c.getLastMsgPreview() == null ? "" : c.getLastMsgPreview())
                .lastMsgAt(c.getLastMsgAt())
                .unreadCount(view.unreadCount())
                .taskTitle(view.taskTitle() == null ? "" : view.taskTitle())
                .taskReward(view.taskRewardCent() / 100.0)
                .archived(c.isArchived())
                .build();
    }
}
