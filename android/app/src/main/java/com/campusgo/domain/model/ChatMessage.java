package com.campusgo.domain.model;

import androidx.annotation.Nullable;

/**
 * C01 聊天消息
 */
public class ChatMessage {

    public enum Type { SYSTEM, TIME, TEXT, IMAGE, VOICE }

    public final Type type;
    public final boolean mine;
    @Nullable
    public final String content;
    @Nullable
    public final String timeLabel;
    @Nullable
    public final String readLabel;
    @Nullable
    public final String voiceDuration;

    private ChatMessage(Type type, boolean mine, @Nullable String content,
                        @Nullable String timeLabel, @Nullable String readLabel,
                        @Nullable String voiceDuration) {
        this.type = type;
        this.mine = mine;
        this.content = content;
        this.timeLabel = timeLabel;
        this.readLabel = readLabel;
        this.voiceDuration = voiceDuration;
    }

    public static ChatMessage system(@Nullable String content) {
        return new ChatMessage(Type.SYSTEM, false, content, null, null, null);
    }

    public static ChatMessage time(@Nullable String timeLabel) {
        return new ChatMessage(Type.TIME, false, null, timeLabel, null, null);
    }

    public static ChatMessage text(boolean mine, @Nullable String content, @Nullable String readLabel) {
        return new ChatMessage(Type.TEXT, mine, content, null, readLabel, null);
    }

    public static ChatMessage image(boolean mine, @Nullable String caption) {
        return new ChatMessage(Type.IMAGE, mine, caption, null, null, null);
    }

    public static ChatMessage voice(boolean mine, @Nullable String duration) {
        return new ChatMessage(Type.VOICE, mine, null, null, null, duration);
    }
}
