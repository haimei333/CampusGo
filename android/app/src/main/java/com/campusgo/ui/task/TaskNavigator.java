package com.campusgo.ui.task;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;

/**
 * 任务相关页面跳转
 */
public final class TaskNavigator {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_VIEWER_ROLE = "viewer_role";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_PUBLISH_TITLE = "publish_title";
    public static final String EXTRA_PUBLISH_MODE = "publish_mode";
    public static final String EXTRA_PUBLISH_REWARD = "publish_reward";
    public static final String EXTRA_PICKUP_ADDRESS = "pickup_address";
    public static final String EXTRA_DELIVERY_ADDRESS = "delivery_address";
    public static final String EXTRA_CANCELLED = "cancelled";
    public static final String EXTRA_PHOTO_UPLOADED = "photo_uploaded";
    public static final String EXTRA_REVIEW_SUB = "review_sub";
    public static final String EXTRA_RUNNER_NAME = "runner_name";
    public static final String EXTRA_COMPLAINT_MODE = "complaint_mode";

    public static final String EXTRA_DRAFT_ID = "draft_id";

    private TaskNavigator() {
    }

    @NonNull
    public static Intent publishWithDraft(@NonNull Context context, @NonNull String draftId) {
        return publish(context).putExtra(EXTRA_DRAFT_ID, draftId);
    }

    @NonNull
    public static Intent templates(@NonNull Context context) {
        return new Intent(context, TemplateActivity.class);
    }

    @Nullable
    public static String parseDraftId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_DRAFT_ID);
    }

    @NonNull
    public static Intent publish(@NonNull Context context) {
        return new Intent(context, PublishActivity.class);
    }

    @NonNull
    public static Intent publishWithCategory(@NonNull Context context, @NonNull TaskCategory category) {
        return publish(context).putExtra(EXTRA_CATEGORY, category.name());
    }

    @NonNull
    public static Intent taskDetail(@NonNull Context context, @NonNull String taskId,
                                    @NonNull UserRole viewerRole) {
        return new Intent(context, TaskDetailActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_VIEWER_ROLE, viewerRole.name());
    }

    @NonNull
    public static Intent taskDetailFromItem(@NonNull Context context, @NonNull TaskListItem item,
                                            @NonNull UserRole viewerRole) {
        return taskDetail(context, item.id, viewerRole)
                .putExtra(EXTRA_STATUS, item.status.name())
                .putExtra(EXTRA_MODE, item.mode.name());
    }

    @NonNull
    public static Intent publishSuccess(@NonNull Context context, @NonNull String title,
                                        @NonNull TaskMode mode, double reward,
                                        @NonNull String pickupAddress,
                                        @NonNull String deliveryAddress,
                                        @Nullable String taskId) {
        Intent intent = new Intent(context, PublishSuccessActivity.class)
                .putExtra(EXTRA_PUBLISH_TITLE, title)
                .putExtra(EXTRA_PUBLISH_MODE, mode.name())
                .putExtra(EXTRA_PUBLISH_REWARD, reward)
                .putExtra(EXTRA_PICKUP_ADDRESS, pickupAddress)
                .putExtra(EXTRA_DELIVERY_ADDRESS, deliveryAddress);
        if (taskId != null) {
            intent.putExtra(EXTRA_TASK_ID, taskId);
        }
        return intent;
    }

    @NonNull
    public static Intent photoConfirm(@NonNull Context context, @NonNull String taskId,
                                      @NonNull UserRole viewerRole) {
        return new Intent(context, PhotoConfirmActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_VIEWER_ROLE, viewerRole.name());
    }

    @NonNull
    public static Intent groupDetail(@NonNull Context context, @NonNull String taskId) {
        return new Intent(context, GroupDetailActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId);
    }

    @NonNull
    public static Intent review(@NonNull Context context, @NonNull String taskId,
                                @NonNull String title, @Nullable String subtitle,
                                double reward, @NonNull UserRole viewerRole) {
        return new Intent(context, ReviewActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_PUBLISH_TITLE, title)
                .putExtra(EXTRA_REVIEW_SUB, subtitle)
                .putExtra(EXTRA_PUBLISH_REWARD, reward)
                .putExtra(EXTRA_VIEWER_ROLE, viewerRole.name());
    }

    @NonNull
    public static Intent tracking(@NonNull Context context, @NonNull String taskId,
                                  @NonNull String title, double reward,
                                  @NonNull String runnerName, @NonNull UserRole viewerRole) {
        return new Intent(context, com.campusgo.ui.location.TrackingActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_PUBLISH_TITLE, title)
                .putExtra(EXTRA_PUBLISH_REWARD, reward)
                .putExtra(EXTRA_RUNNER_NAME, runnerName)
                .putExtra(EXTRA_VIEWER_ROLE, viewerRole.name());
    }

    @NonNull
    public static Intent heatmap(@NonNull Context context) {
        return new Intent(context, com.campusgo.ui.location.HeatmapActivity.class);
    }

    @NonNull
    public static Intent complaint(@NonNull Context context, @NonNull String taskId,
                                   @NonNull String title, double reward,
                                   @NonNull String mode) {
        return new Intent(context, com.campusgo.ui.complaint.ComplaintActivity.class)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_PUBLISH_TITLE, title)
                .putExtra(EXTRA_PUBLISH_REWARD, reward)
                .putExtra(EXTRA_COMPLAINT_MODE, mode);
    }

    @Nullable
    public static TaskCategory parseCategory(@Nullable Intent intent) {
        if (intent == null || intent.getStringExtra(EXTRA_CATEGORY) == null) {
            return null;
        }
        try {
            return TaskCategory.valueOf(intent.getStringExtra(EXTRA_CATEGORY));
        } catch (Exception e) {
            return null;
        }
    }
}
