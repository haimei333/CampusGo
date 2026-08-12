package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.R;
import com.campusgo.databinding.ActivityPhotoConfirmBinding;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;

/**
 * T08 拍照确认（演示：模拟取景与上传）
 */
public class PhotoConfirmActivity extends AppCompatActivity {

    private ActivityPhotoConfirmBinding binding;
    private String taskId;
    private UserRole viewerRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoConfirmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        viewerRole = parseRole(getIntent().getStringExtra(TaskNavigator.EXTRA_VIEWER_ROLE));

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackPreview.setOnClickListener(v -> showCamera());
        binding.btnCapture.setOnClickListener(v -> showPreview());
        binding.btnRetake.setOnClickListener(v -> showCamera());
        binding.btnUpload.setOnClickListener(v -> uploadPhoto());
    }

    private void showPreview() {
        binding.panelCamera.setVisibility(View.GONE);
        binding.panelPreview.setVisibility(View.VISIBLE);
        binding.panelUploading.setVisibility(View.GONE);
    }

    private void showCamera() {
        binding.panelCamera.setVisibility(View.VISIBLE);
        binding.panelPreview.setVisibility(View.GONE);
        binding.panelUploading.setVisibility(View.GONE);
    }

    private void uploadPhoto() {
        binding.panelPreview.setVisibility(View.GONE);
        binding.panelUploading.setVisibility(View.VISIBLE);
        binding.getRoot().postDelayed(() -> {
            Intent data = new Intent()
                    .putExtra(TaskNavigator.EXTRA_TASK_ID, taskId)
                    .putExtra(TaskNavigator.EXTRA_PHOTO_UPLOADED, true)
                    .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.CONFIRMING.name());
            setResult(RESULT_OK, data);
            Toast.makeText(this, R.string.photo_upload_success, Toast.LENGTH_SHORT).show();
            finish();
        }, 900);
    }

    @Nullable
    private UserRole parseRole(@Nullable String raw) {
        if (raw == null) {
            return UserRole.RUNNER;
        }
        try {
            return UserRole.valueOf(raw);
        } catch (Exception e) {
            return UserRole.RUNNER;
        }
    }
}
