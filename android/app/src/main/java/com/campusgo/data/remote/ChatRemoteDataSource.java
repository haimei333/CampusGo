package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.api.ChatApi;
import com.campusgo.data.remote.dto.chat.ChatMessageDto;
import com.campusgo.data.remote.dto.chat.ConversationDto;
import com.campusgo.data.remote.dto.chat.SendMessageRequest;
import com.campusgo.domain.model.ChatMessage;
import com.campusgo.domain.model.ChatThread;
import com.campusgo.domain.model.UserRole;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatRemoteDataSource {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA);

    private final ChatApi chatApi;

    public ChatRemoteDataSource(@NonNull ChatApi chatApi) {
        this.chatApi = chatApi;
    }

    public void listConversations(@NonNull ApiCallback<List<ChatThread>> callback) {
        ApiExecutor.enqueue(chatApi.list(), new ApiCallback<List<ConversationDto>>() {
            @Override
            public void onSuccess(List<ConversationDto> data) {
                List<ChatThread> threads = new ArrayList<>();
                if (data != null) {
                    for (ConversationDto dto : data) {
                        threads.add(toThread(dto));
                    }
                }
                callback.onSuccess(threads);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void getOrCreateByTask(@NonNull String taskId, @NonNull ApiCallback<ChatThread> callback) {
        ApiExecutor.enqueue(chatApi.byTask(taskId), new ApiCallback<ConversationDto>() {
            @Override
            public void onSuccess(ConversationDto data) {
                callback.onSuccess(toThread(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void listMessages(@NonNull String conversationId, @NonNull ApiCallback<List<ChatMessage>> callback) {
        ApiExecutor.enqueue(chatApi.messages(conversationId, null, 50),
                new ApiCallback<List<ChatMessageDto>>() {
                    @Override
                    public void onSuccess(List<ChatMessageDto> data) {
                        callback.onSuccess(mapMessages(data));
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void sendText(@NonNull String conversationId, @NonNull String content,
                         @NonNull ApiCallback<ChatMessage> callback) {
        ApiExecutor.enqueue(chatApi.send(conversationId, new SendMessageRequest(content)),
                new ApiCallback<ChatMessageDto>() {
                    @Override
                    public void onSuccess(ChatMessageDto data) {
                        callback.onSuccess(toMessage(data));
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void markRead(@NonNull String conversationId, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(chatApi.markRead(conversationId), callback);
    }

    @NonNull
    private static ChatThread toThread(@Nullable ConversationDto dto) {
        if (dto == null) {
            return new ChatThread("", "同学", null, "", "", 0, null);
        }
        UserRole peerRole = null;
        if (dto.peerRole != null) {
            try {
                peerRole = UserRole.valueOf(dto.peerRole);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new ChatThread(
                dto.id != null ? dto.id : "",
                dto.peerName != null ? dto.peerName : "同学",
                peerRole,
                dto.preview != null ? dto.preview : "",
                formatRelative(dto.lastMsgAt),
                dto.unreadCount,
                dto.taskTitle,
                dto.taskId,
                dto.taskReward,
                dto.archived);
    }

    @NonNull
    private static List<ChatMessage> mapMessages(@Nullable List<ChatMessageDto> data) {
        List<ChatMessage> list = new ArrayList<>();
        if (data == null) {
            return list;
        }
        Instant lastBucket = null;
        for (ChatMessageDto dto : data) {
            Instant created = parseInstant(dto.createdAt);
            if (created != null) {
                Instant bucket = created.truncatedTo(ChronoUnit.MINUTES);
                if (lastBucket == null || !bucket.equals(lastBucket)) {
                    list.add(ChatMessage.time(formatClock(created)));
                    lastBucket = bucket;
                }
            }
            list.add(toMessage(dto));
        }
        return list;
    }

    @NonNull
    private static ChatMessage toMessage(@Nullable ChatMessageDto dto) {
        if (dto == null) {
            return ChatMessage.text(true, "", null);
        }
        String type = dto.msgType != null ? dto.msgType.toUpperCase(Locale.ROOT) : "TEXT";
        if ("SYSTEM".equals(type)) {
            return ChatMessage.system(dto.content);
        }
        if ("IMAGE".equals(type)) {
            return ChatMessage.image(dto.mine, dto.content);
        }
        if ("VOICE".equals(type)) {
            return ChatMessage.voice(dto.mine, dto.content);
        }
        String readLabel = dto.mine && dto.read ? "已读" : null;
        return ChatMessage.text(dto.mine, dto.content, readLabel);
    }

    @Nullable
    private static Instant parseInstant(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    private static String formatClock(@NonNull Instant instant) {
        return TIME_FMT.format(instant.atZone(ZoneId.systemDefault()));
    }

    @NonNull
    private static String formatRelative(@Nullable String raw) {
        Instant instant = parseInstant(raw);
        if (instant == null) {
            return "";
        }
        long minutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "小时前";
        }
        long days = hours / 24;
        if (days == 1) {
            return "昨天";
        }
        if (days < 7) {
            return days + "天前";
        }
        return formatClock(instant);
    }
}
