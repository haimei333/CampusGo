package com.campusgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
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
import com.campusgo.databinding.ActivityLoginBinding;
import com.campusgo.ui.main.MainActivity;

/**
 * A01 登录 — 手机号 + 密码（须先注册）。
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private boolean loggingIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        if (sessionManager.isLoggedIn()) {
            goMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupAgreementText();
        setupWatchers();
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> updateLoginButtonState());
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

    private void setupWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        binding.etPhone.addTextChangedListener(watcher);
        binding.etPassword.addTextChangedListener(watcher);
    }

    private void updateLoginButtonState() {
        binding.btnLogin.setEnabled(!loggingIn);
    }

    private void attemptLogin() {
        if (loggingIn) {
            return;
        }
        if (!binding.cbAgree.isChecked()) {
            showLoginError(getString(R.string.login_need_agree));
            return;
        }

        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        String clientError = validateInput(phone, password);
        if (clientError != null) {
            showLoginError(clientError);
            return;
        }

        if (FeatureFlags.USE_REMOTE_API) {
            remoteLogin(phone, password);
        } else {
            mockLogin(phone, password);
        }
    }

    @Nullable
    private String validateInput(@NonNull String phone, @NonNull String password) {
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
        return null;
    }

    private void mockLogin(@NonNull String phone, @NonNull String password) {
        Boolean ok = sessionManager.verifyLocalAccount(phone, password);
        if (ok == null) {
            showLoginError(getString(R.string.login_error_not_registered));
            return;
        }
        if (!ok) {
            showLoginError(getString(R.string.login_error_password_wrong));
            return;
        }
        sessionManager.login(phone, "mock-token-" + System.currentTimeMillis());
        navigateAfterLogin();
    }

    private void remoteLogin(@NonNull String phone, @NonNull String password) {
        setLoggingIn(true);
        RetrofitClient.get().authRemote().login(phone, password, new ApiCallback<Void>() {
            @Override
            public void onSuccess(@NonNull Void data) {
                runOnUiThread(() -> {
                    setLoggingIn(false);
                    navigateAfterLogin();
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> {
                    setLoggingIn(false);
                    showLoginError(resolveRemoteError(error));
                });
            }
        });
    }

    @NonNull
    private String resolveRemoteError(@NonNull ApiException error) {
        String message = error.getMessage();
        if (message == null) {
            message = "";
        }
        if (error.getCode() == -1 && message.contains("网络")) {
            return getString(R.string.login_error_network);
        }
        if (message.contains("不存在") || message.contains("先注册")) {
            return getString(R.string.login_error_not_registered);
        }
        if (message.contains("密码") || message.contains("手机号或密码")) {
            return message.contains("尚未设置")
                    ? message
                    : getString(R.string.login_error_password_wrong);
        }
        if (message.contains("手机号")) {
            return getString(R.string.login_error_phone_format);
        }
        if (error.getCode() >= 400 && error.getCode() < 600) {
            return getString(R.string.login_error_server, error.getCode());
        }
        if (!message.isEmpty()) {
            return getString(R.string.login_error_unknown, message);
        }
        return getString(R.string.login_error_password_wrong);
    }

    private void setLoggingIn(boolean inProgress) {
        loggingIn = inProgress;
        binding.btnLogin.setEnabled(!inProgress);
        binding.btnLogin.setText(inProgress ? R.string.login_submitting : R.string.login_submit);
    }

    private void showLoginError(@NonNull String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.login_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.login_dialog_ok, null)
                .show();
    }

    private void navigateAfterLogin() {
        if (!sessionManager.isGuideShown()) {
            startActivity(AuthNavigator.guide(this));
        } else {
            goMain();
        }
        finish();
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
