package com.campusgo.ui.location;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.databinding.ActivityTrackingBinding;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.chat.ChatNavigator;
import com.campusgo.ui.task.TaskNavigator;

/**
 * L01 配送追踪
 */
public class TrackingActivity extends AppCompatActivity {

    private ActivityTrackingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        String runnerName = getIntent().getStringExtra(TaskNavigator.EXTRA_RUNNER_NAME);
        String title = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_TITLE);
        double reward = getIntent().getDoubleExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, 15);
        UserRole role = parseRole(getIntent().getStringExtra(TaskNavigator.EXTRA_VIEWER_ROLE));

        if (runnerName == null || runnerName.isEmpty()) {
            runnerName = "张同学";
        }
        final String peerName = runnerName;
        final String taskTitle = title != null ? title : "取快递";
        final String taskIdFinal = taskId != null ? taskId : "t1";
        String initial = peerName.substring(0, 1);
        binding.tvRunnerAvatar.setText(initial);
        binding.tvRunnerName.setText(peerName);
        binding.tvEta.setText(getString(com.campusgo.R.string.tracking_eta_value));

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnContact.setOnClickListener(v ->
                startActivity(ChatNavigator.fromTask(
                        this,
                        taskIdFinal,
                        peerName,
                        taskTitle,
                        reward,
                        role)));
    }

    private UserRole parseRole(@Nullable String raw) {
        if (raw == null) {
            return UserRole.PUBLISHER;
        }
        try {
            return UserRole.valueOf(raw);
        } catch (Exception e) {
            return UserRole.PUBLISHER;
        }
    }
}
