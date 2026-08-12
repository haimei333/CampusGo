package com.campusgo.ui.message;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockChatRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.domain.model.AppNotification;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.chat.ChatNavigator;
import com.campusgo.ui.profile.ProfileNavigator;
import com.campusgo.ui.task.TaskNavigator;

/**
 * M03 通知点击跳转
 */
public final class NotificationNavigator {

    private NotificationNavigator() {
    }

    public static void open(@NonNull Context context,
                            @NonNull AppNotification notification,
                            @NonNull SessionManager session) {
        UserRole role = session.getActiveRole();
        if (FeatureFlags.USE_REMOTE_API && shouldRefreshWallet(notification)) {
            refreshWalletQuietly();
        }
        switch (notification.linkType) {
            case TASK:
                if (notification.linkTargetId == null) {
                    return;
                }
                TaskStatus status = notification.taskStatus != null
                        ? notification.taskStatus
                        : TaskStatus.PENDING;
                Intent taskIntent = TaskNavigator.taskDetail(context, notification.linkTargetId, role)
                        .putExtra(TaskNavigator.EXTRA_STATUS, status.name());
                if (notification.taskMode != null) {
                    taskIntent.putExtra(TaskNavigator.EXTRA_MODE, notification.taskMode.name());
                }
                context.startActivity(taskIntent);
                break;
            case REVIEW:
                if (notification.linkTargetId == null) {
                    return;
                }
                String title = notification.chatTaskTitle != null && !notification.chatTaskTitle.isEmpty()
                        ? notification.chatTaskTitle
                        : notification.title;
                context.startActivity(TaskNavigator.review(
                        context,
                        notification.linkTargetId,
                        title,
                        notification.body,
                        0,
                        role));
                break;
            case GROUP:
                if (notification.linkTargetId != null) {
                    context.startActivity(TaskNavigator.groupDetail(context, notification.linkTargetId));
                }
                break;
            case CHAT:
                if (notification.linkTargetId == null) {
                    return;
                }
                String peer = notification.chatPeerName != null ? notification.chatPeerName : "用户";
                String taskId = notification.chatTaskId != null
                        ? notification.chatTaskId
                        : MockChatRepository.taskIdForThread(notification.linkTargetId);
                context.startActivity(ChatNavigator.open(
                        context,
                        notification.linkTargetId,
                        peer,
                        taskId,
                        notification.chatTaskTitle,
                        15.0,
                        role,
                        false));
                break;
            case HELP:
                context.startActivity(ProfileNavigator.help(context));
                break;
            case NONE:
            default:
                break;
        }
    }

    private static boolean shouldRefreshWallet(@NonNull AppNotification notification) {
        if ("TASK_COMPLETED".equals(notification.bizType)) {
            return true;
        }
        return notification.linkType == AppNotification.LinkType.TASK
                && notification.title != null
                && notification.title.contains("已完成");
    }

    private static void refreshWalletQuietly() {
        RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<com.campusgo.data.remote.dto.wallet.WalletResponse>() {
            @Override
            public void onSuccess(com.campusgo.data.remote.dto.wallet.WalletResponse data) {
            }

            @Override
            public void onError(@NonNull ApiException error) {
            }
        });
    }
}
