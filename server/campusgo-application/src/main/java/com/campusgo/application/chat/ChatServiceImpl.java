package com.campusgo.application.chat;

import com.campusgo.domain.enums.ChatMsgType;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.ChatConversation;
import com.campusgo.domain.model.ChatMessage;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.ChatConversationRepository;
import com.campusgo.domain.repository.ChatMessageRepository;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.application.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int DEFAULT_PAGE = 50;
    private static final int MAX_PAGE = 100;
    private static final String SYSTEM_WELCOME = "任务已被接单，你们可以开始沟通了";

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> listConversations(long userId) {
        return conversationRepository.listByUserId(userId).stream()
                .map(c -> toView(c, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationView getConversation(long userId, long conversationId) {
        ChatConversation conversation = requireMember(userId, conversationId);
        return toView(conversation, userId);
    }

    @Override
    @Transactional
    public ConversationView getOrCreateByTask(long userId, long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在"));
        if (task.getRunnerId() == null) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "任务尚未接单，暂不可聊天");
        }
        if (userId != task.getPublisherId() && userId != task.getRunnerId()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权进入该任务会话");
        }
        ChatConversation conversation = ensureForAcceptedTask(taskId, task.getPublisherId(), task.getRunnerId());
        return toView(conversation, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> listMessages(long userId, long conversationId, Long beforeId, int limit) {
        requireMember(userId, conversationId);
        int size = limit <= 0 ? DEFAULT_PAGE : Math.min(limit, MAX_PAGE);
        return messageRepository.listByConversation(conversationId, beforeId, size);
    }

    @Override
    @Transactional
    public ChatMessage sendText(long userId, long conversationId, String content) {
        ChatConversation conversation = requireMember(userId, conversationId);
        if (conversation.isArchived()) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "会话已归档，无法发送");
        }
        if (!StringUtils.hasText(content)) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "消息不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 2000) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "消息不能超过 2000 字");
        }

        Instant now = Instant.now();
        ChatMessage saved = messageRepository.save(ChatMessage.builder()
                .conversationId(conversationId)
                .taskId(conversation.getTaskId())
                .senderId(userId)
                .msgType(ChatMsgType.TEXT)
                .content(trimmed)
                .read(false)
                .createdAt(now)
                .build());

        String preview = trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
        conversationRepository.save(conversation.toBuilder()
                .lastMsgPreview(preview)
                .lastMsgAt(now)
                .updatedAt(now)
                .build());

        long peerId = userId == conversation.getPublisherId()
                ? conversation.getRunnerId()
                : conversation.getPublisherId();
        String senderName = userRepository.findById(userId).map(UserProfile::getNickname).orElse("同学");
        Task task = taskRepository.findById(conversation.getTaskId()).orElse(null);
        String taskTitle = task != null ? task.getTitle() : "";
        notificationService.notifyChatMessage(
                peerId, conversationId, conversation.getTaskId(), senderName, taskTitle, preview);
        return saved;
    }

    @Override
    @Transactional
    public void markRead(long userId, long conversationId) {
        requireMember(userId, conversationId);
        messageRepository.markReadByPeer(conversationId, userId);
    }

    @Override
    @Transactional
    public ChatConversation ensureForAcceptedTask(long taskId, long publisherId, long runnerId) {
        return conversationRepository.findByTaskId(taskId).orElseGet(() -> {
            Instant now = Instant.now();
            ChatConversation created = conversationRepository.save(ChatConversation.builder()
                    .taskId(taskId)
                    .publisherId(publisherId)
                    .runnerId(runnerId)
                    .lastMsgPreview(SYSTEM_WELCOME)
                    .lastMsgAt(now)
                    .archived(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            messageRepository.save(ChatMessage.builder()
                    .conversationId(created.getId())
                    .taskId(taskId)
                    .senderId(null)
                    .msgType(ChatMsgType.SYSTEM)
                    .content(SYSTEM_WELCOME)
                    .read(true)
                    .createdAt(now)
                    .build());
            return created;
        });
    }

    private ChatConversation requireMember(long userId, long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "会话不存在"));
        if (userId != conversation.getPublisherId() && userId != conversation.getRunnerId()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权访问该会话");
        }
        return conversation;
    }

    private ConversationView toView(ChatConversation conversation, long userId) {
        boolean iAmPublisher = userId == conversation.getPublisherId();
        long peerId = iAmPublisher ? conversation.getRunnerId() : conversation.getPublisherId();
        String peerRole = iAmPublisher ? UserRole.RUNNER.name() : UserRole.PUBLISHER.name();
        String peerNickname = userRepository.findById(peerId)
                .map(UserProfile::getNickname)
                .orElse("同学");

        Task task = taskRepository.findById(conversation.getTaskId()).orElse(null);
        String taskTitle = task != null ? task.getTitle() : "";
        int rewardCent = task != null ? task.getRewardCent() : 0;
        int unread = messageRepository.countUnread(conversation.getId(), userId);

        return new ConversationView(conversation, peerNickname, peerRole, taskTitle, rewardCent, unread);
    }
}
