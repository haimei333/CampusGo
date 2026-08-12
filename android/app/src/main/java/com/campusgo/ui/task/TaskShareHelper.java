package com.campusgo.ui.task;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.campusgo.R;
import com.campusgo.domain.model.TaskDetail;

/**
 * 任务分享（系统分享面板）
 */
public final class TaskShareHelper {

    private TaskShareHelper() {
    }

    public static void share(@NonNull Context context, @NonNull TaskDetail task) {
        shareText(context, task.title, task.reward, task.pickupAddress, task.deliveryAddress);
    }

    public static void sharePublish(@NonNull Context context,
                                      @NonNull String title,
                                      double reward,
                                      @NonNull String pickupAddress,
                                      @NonNull String deliveryAddress) {
        shareText(context, title, reward, pickupAddress, deliveryAddress);
    }

    private static void shareText(@NonNull Context context,
                                  @NonNull String title,
                                  double reward,
                                  @NonNull String pickupAddress,
                                  @NonNull String deliveryAddress) {
        String message = context.getString(
                R.string.task_share_message,
                title,
                String.format("¥%.2f", reward),
                pickupAddress,
                deliveryAddress);
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.task_share_subject))
                .putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(
                intent, context.getString(R.string.task_share_chooser)));
    }
}
