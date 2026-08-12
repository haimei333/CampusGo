package com.campusgo.ui.chat;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.UserRole;

/**
 * C01 聊天页跳转
 */
public final class ChatNavigator {

    public static final String EXTRA_THREAD_ID = "thread_id";
    public static final String EXTRA_PEER_NAME = "peer_name";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_REWARD = "task_reward";
    public static final String EXTRA_VIEWER_ROLE = "viewer_role";
    public static final String EXTRA_ARCHIVED = "archived";

    private ChatNavigator() {
    }

    @NonNull
    public static Intent open(@NonNull Context context, @NonNull String threadId,
                              @NonNull String peerName, @Nullable String taskId,
                              @Nullable String taskTitle, double taskReward,
                              @NonNull UserRole viewerRole, boolean archived) {
        return new Intent(context, ChatActivity.class)
                .putExtra(EXTRA_THREAD_ID, threadId)
                .putExtra(EXTRA_PEER_NAME, peerName)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_TASK_TITLE, taskTitle)
                .putExtra(EXTRA_TASK_REWARD, taskReward)
                .putExtra(EXTRA_VIEWER_ROLE, viewerRole.name())
                .putExtra(EXTRA_ARCHIVED, archived);
    }

    @NonNull
    public static Intent fromTask(@NonNull Context context, @NonNull String taskId,
                                  @NonNull String peerName, @NonNull String taskTitle,
                                  double taskReward, @NonNull UserRole viewerRole) {
        return open(context, "task-" + taskId, peerName, taskId, taskTitle, taskReward, viewerRole, false);
    }
}
