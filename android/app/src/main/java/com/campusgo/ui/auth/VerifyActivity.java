package com.campusgo.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityVerifyBinding;

import androidx.annotation.NonNull;

/**
 * S01 校园卡认证
 */
public class VerifyActivity extends AppCompatActivity {

    private ActivityVerifyBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submit());

        refreshVerifiedUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().userRemote().loadProfile(new ApiCallback<>() {
                @Override
                public void onSuccess(@NonNull com.campusgo.data.remote.dto.user.UserProfileDto data) {
                    runOnUiThread(VerifyActivity.this::refreshVerifiedUi);
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    // 忽略：仍可使用表单提交
                }
            });
        }
    }

    private void refreshVerifiedUi() {
        if (sessionManager.isCampusVerified()) {
            binding.tvBanner.setText(R.string.verify_done_banner);
            binding.btnSubmit.setText(R.string.verify_done_btn);
        } else {
            binding.tvBanner.setText(R.string.verify_banner);
            binding.btnSubmit.setText(R.string.verify_submit);
        }
    }

    private void submit() {
        if (sessionManager.isCampusVerified()) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(binding.etName.getText().toString().trim())
                || TextUtils.isEmpty(binding.etStudentId.getText().toString().trim())) {
            Toast.makeText(this, R.string.verify_need_info, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = binding.etName.getText().toString().trim();
        String studentId = binding.etStudentId.getText().toString().trim();
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnSubmit.setEnabled(false);
            RetrofitClient.get().userRemote().submitCampusAuth(name, studentId, new ApiCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    runOnUiThread(() -> {
                        Toast.makeText(VerifyActivity.this, R.string.verify_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(VerifyActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        sessionManager.setCampusVerified(true);
        Toast.makeText(this, R.string.verify_success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
