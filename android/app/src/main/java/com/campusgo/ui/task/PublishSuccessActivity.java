package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.R;
import com.campusgo.databinding.ActivityPublishSuccessBinding;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.main.MainActivity;

/**
 * T02 发布成功
 */
public class PublishSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPublishSuccessBinding binding = ActivityPublishSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String titleRaw = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_TITLE);
        final String title = titleRaw == null ? "" : titleRaw;
        TaskMode mode = TaskMode.NORMAL;
        try {
            String modeRaw = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_MODE);
            if (modeRaw != null) {
                mode = TaskMode.valueOf(modeRaw);
            }
        } catch (Exception ignored) {
        }
        final TaskMode publishMode = mode;
        double reward = getIntent().getDoubleExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, 0);
        String pickup = getIntent().getStringExtra(TaskNavigator.EXTRA_PICKUP_ADDRESS);
        String delivery = getIntent().getStringExtra(TaskNavigator.EXTRA_DELIVERY_ADDRESS);
        if (pickup == null) {
            pickup = getString(R.string.publish_address_unset);
        }
        if (delivery == null) {
            delivery = getString(R.string.publish_address_unset);
        }
        final String pickupAddress = pickup;
        final String deliveryAddress = delivery;

        String statusLabel = publishMode == TaskMode.GROUP
                ? getString(R.string.publish_success_status_group)
                : getString(R.string.publish_success_status_pending);
        binding.tvSummary.setText(getString(R.string.publish_success_summary,
                title, statusLabel, String.format("¥%.2f", reward)));

        binding.btnViewTask.setText(publishMode == TaskMode.GROUP
                ? R.string.publish_success_view_group
                : R.string.publish_success_view);

        binding.btnShare.setOnClickListener(v ->
                TaskShareHelper.sharePublish(this, title, reward, pickupAddress, deliveryAddress));

        binding.btnViewTask.setOnClickListener(v -> {
            if (publishMode == TaskMode.GROUP) {
                startActivity(TaskNavigator.groupDetail(this, "p3"));
            } else {
                String taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
                if (taskId != null && !taskId.isEmpty()) {
                    startActivity(TaskNavigator.taskDetail(this, taskId, UserRole.PUBLISHER)
                            .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.PENDING.name())
                            .putExtra(TaskNavigator.EXTRA_MODE, publishMode.name()));
                } else {
                    startActivity(TaskNavigator.taskDetail(this, "new-" + System.currentTimeMillis(), UserRole.PUBLISHER)
                            .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.PENDING.name())
                            .putExtra(TaskNavigator.EXTRA_MODE, publishMode.name())
                            .putExtra(TaskNavigator.EXTRA_PUBLISH_TITLE, title)
                            .putExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, reward));
                }
            }
            finish();
        });

        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
