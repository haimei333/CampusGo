package com.campusgo.domain.model;

import androidx.annotation.Nullable;

/**
 * M03 聊天列表项
 */
public class ChatThread {

    public final String id;
    public final String peerName;
    /** 对方身份；系统/助手等为 null，不展示标签 */
    @Nullable
    public final UserRole peerRole;
    public final String preview;
    public final String timeLabel;
    public final int unreadCount;
    @Nullable
    public final String relatedTask;
    @Nullable
    public final String taskId;
    public final double taskReward;
    public final boolean archived;

    public ChatThread(String id, String peerName, @Nullable UserRole peerRole,
                      String preview, String timeLabel, int unreadCount,
                      @Nullable String relatedTask) {
        this(id, peerName, peerRole, preview, timeLabel, unreadCount, relatedTask, null, 0, peerRole == null);
    }

    public ChatThread(String id, String peerName, @Nullable UserRole peerRole,
                      String preview, String timeLabel, int unreadCount,
                      @Nullable String relatedTask, @Nullable String taskId,
                      double taskReward, boolean archived) {
        this.id = id;
        this.peerName = peerName;
        this.peerRole = peerRole;
        this.preview = preview;
        this.timeLabel = timeLabel;
        this.unreadCount = unreadCount;
        this.relatedTask = relatedTask;
        this.taskId = taskId;
        this.taskReward = taskReward;
        this.archived = archived;
    }
}
