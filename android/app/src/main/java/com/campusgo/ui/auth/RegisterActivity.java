package com.campusgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityRegisterBinding;
import com.campusgo.ui.main.MainActivity;

/**
 * 注册 — 手机号 + 密码；成功后自动登录。
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private SessionManager sessionManager;
    private boolean registering;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        if (sessionManager.isLoggedIn()) {
            goMain();
            return;
        }

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupAgreementText();
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoLogin.setOnClickListener(v -> finish());
    }

    private void setupAgreementText() {
        String full = getString(R.string.login_user_agreement)
                + getString(R.string.login_and)
                + getString(R.string.login_privacy);
        SpannableString ss = new SpannableString(full);
        int brand = ContextCompat.getColor(this, R.color.cg_brand);
        ss.setSpan(new ForegroundColorSpan(brand), 0, getString(R.string.login_user_agreement).length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int startPrivacy = full.indexOf(getString(R.string.login_privacy));
        ss.setSpan(new ForegroundColorSpan(brand), startPrivacy, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvAgreement.setText(ss);
    }

    private void attemptRegister() {
        if (registering) {
            return;
        }
        if (!binding.cbAgree.isChecked()) {
            showError(getString(R.string.login_need_agree));
            return;
        }

        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirm = binding.etPasswordConfirm.getText().toString();

        String error = validate(phone, password, confirm);
        if (error != null) {
            showError(error);
            return;
        }

        if (FeatureFlags.USE_REMOTE_API) {
            remoteRegister(phone, password);
        } else {
            mockRegister(phone, password);
        }
    }

    @Nullable
    private String validate(@NonNull String phone, @NonNull String password, @NonNull String confirm) {
        if (phone.isEmpty()) {
            return getString(R.string.login_error_phone_empty);
        }
        if (phone.length() != 11) {
            return getString(R.string.login_error_phone_length);
        }
        if (!phone.matches("^1\\d{10}$")) {
            return getString(R.string.login_error_phone_format);
        }
        if (password.isEmpty()) {
            return getString(R.string.login_error_password_empty);
        }
        if (password.length() < 6) {
            return getString(R.string.login_error_password_length);
        }
        if (password.length() > 32) {
            return getString(R.string.login_error_password_too_long);
        }
        if (confirm.isEmpty()) {
            return getString(R.string.register_error_confirm_empty);
        }
        if (!password.equals(confirm)) {
            return getString(R.string.register_error_confirm_mismatch);
        }
        return null;
    }

    private void mockRegister(@NonNull String phone, @NonNull String password) {
        if (!sessionManager.registerLocalAccount(phone, password)) {
            showError(getString(R.string.register_error_already_exists));
            return;
        }
        sessionManager.login(phone, "mock-token-" + System.currentTimeMillis());
        navigateAfterAuth();
    }

    private void remoteRegister(@NonNull String phone, @NonNull String password) {
        setRegistering(true);
        RetrofitClient.get().authRemote().register(phone, password, new ApiCallback<Void>() {
            @Override
            public void onSuccess(@NonNull Void data) {
                runOnUiThread(() -> {
                    setRegistering(false);
                    navigateAfterAuth();
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> {
                    setRegistering(false);
                    showError(resolveError(error));
                });
            }
        });
    }

    @NonNull
    private String resolveError(@NonNull ApiException error) {
        String message = error.getMessage();
        if (message == null) {
            message = "";
        }
        if (error.getCode() == -1 && message.contains("网络")) {
            return getString(R.string.login_error_network);
        }
        if (message.contains("已注册")) {
            return getString(R.string.register_error_already_exists);
        }
        if (message.contains("手机号")) {
            return getString(R.string.login_error_phone_format);
        }
        if (message.contains("密码")) {
            return message;
        }
        if (!message.isEmpty()) {
            return getString(R.string.register_error_unknown, message);
        }
        return getString(R.string.register_error_unknown, "请稍后重试");
    }

    private void setRegistering(boolean inProgress) {
        registering = inProgress;
        binding.btnRegister.setEnabled(!inProgress);
        binding.btnRegister.setText(inProgress ? R.string.register_submitting : R.string.register_submit);
    }

    private void showError(@NonNull String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.register_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.login_dialog_ok, null)
                .show();
    }

    private void navigateAfterAuth() {
        if (!sessionManager.isGuideShown()) {
            startActivity(AuthNavigator.guide(this));
        } else {
            goMain();
        }
        finish();
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
