package com.campusgo.data.mock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * C01 演示聊天消息
 */
public final class MockChatRepository {

    private static final Map<String, List<ChatMessage>> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put("c1", defaultExpressChat());
        MESSAGES.put("c2", defaultFileChat());
        MESSAGES.put("c4", defaultReportChat());
        MESSAGES.put("c5", defaultExpressChat());
    }

    private MockChatRepository() {
    }

    @NonNull
    public static List<ChatMessage> messages(@NonNull String threadId) {
        List<ChatMessage> list = MESSAGES.get(threadId);
        if (list != null) {
            return new ArrayList<>(list);
        }
        return defaultExpressChat();
    }

    public static void append(@NonNull String threadId, @NonNull ChatMessage message) {
        List<ChatMessage> list = MESSAGES.computeIfAbsent(threadId, k -> new ArrayList<>());
        list.add(message);
    }

    @Nullable
    public static String taskIdForThread(@NonNull String threadId) {
        switch (threadId) {
            case "c1":
            case "c5":
                return "t1";
            case "c2":
                return "h3";
            case "c4":
                return "h3";
            default:
                return "demo";
        }
    }

    @NonNull
    private static List<ChatMessage> defaultExpressChat() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(ChatMessage.system("任务已被接单，你们可以开始沟通了"));
        list.add(ChatMessage.time("今天 14:30"));
        list.add(ChatMessage.text(false, "你好，我已经到快递站了，请问是哪个快递？", null));
        list.add(ChatMessage.text(true, "中通快递，取件码 3-2-4567", "已读"));
        list.add(ChatMessage.text(false, "好的，找到了，我现在送过去", null));
        list.add(ChatMessage.text(true, "谢谢！", "已读"));
        list.add(ChatMessage.time("今天 14:45"));
        list.add(ChatMessage.text(false, "我已送达，请确认收货", null));
        return list;
    }

    @NonNull
    private static List<ChatMessage> defaultFileChat() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(ChatMessage.system("任务已被接单，你们可以开始沟通了"));
        list.add(ChatMessage.time("今天 10:20"));
        list.add(ChatMessage.text(false, "文件已打印好，我现在送过去", null));
        list.add(ChatMessage.text(true, "好的，行政楼 3 层前台", "已读"));
        list.add(ChatMessage.image(false, "送达照片"));
        return list;
    }

    @NonNull
    private static List<ChatMessage> defaultReportChat() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(ChatMessage.time("昨天 16:00"));
        list.add(ChatMessage.text(true, "开题报告麻烦今天帮忙打印", null));
        list.add(ChatMessage.text(false, "收到，预计明天上午送到", "已读"));
        list.add(ChatMessage.voice(false, "12″"));
        return list;
    }
}
