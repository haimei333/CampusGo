package com.campusgo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.databinding.ActivitySecurityBinding;
import com.campusgo.ui.auth.AuthNavigator;

/**
 * S04 安全中心
 */
public class SecurityActivity extends AppCompatActivity {

    private ActivitySecurityBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChangePhone.setOnClickListener(v -> showChangePhoneDialog());
        binding.rowDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        binding.rowLogout.setOnClickListener(v -> confirmLogout());

        renderAccount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderAccount();
    }

    private void renderAccount() {
        binding.tvPhone.setText(maskPhone(sessionManager.getPhone()));
        if (sessionManager.isCampusVerified()) {
            binding.tvSecurityLevel.setText(R.string.security_level_high);
            binding.tvSecurityLevel.setTextColor(getColor(R.color.cg_success));
        } else {
            binding.tvSecurityLevel.setText(R.string.security_level_medium);
            binding.tvSecurityLevel.setTextColor(getColor(R.color.cg_warning));
        }
    }

    private void showChangePhoneDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint(R.string.security_change_phone_hint);
        String current = sessionManager.getPhone();
        if (current != null) {
            input.setText(current);
        }
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(R.string.security_change_phone_title)
                .setMessage(R.string.security_change_phone_msg)
                .setView(input)
                .setPositiveButton(R.string.security_change_phone_confirm, (d, w) -> {
                    String phone = input.getText().toString().trim();
                    if (phone.length() < 11) {
                        Toast.makeText(this, R.string.login_invalid_phone, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionManager.setPhone(phone);
                    Toast.makeText(this, R.string.security_change_phone_success, Toast.LENGTH_SHORT).show();
                    renderAccount();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.security_delete_account)
                .setMessage(R.string.security_delete_confirm)
                .setPositiveButton(R.string.security_delete_confirm_btn, (d, w) -> {
                    Toast.makeText(this, R.string.security_delete_success, Toast.LENGTH_SHORT).show();
                    AuthNavigator.performLogout(this, sessionManager);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_logout_title)
                .setMessage(R.string.settings_logout_message)
                .setPositiveButton(R.string.settings_logout_confirm, (d, w) ->
                        AuthNavigator.performLogout(this, sessionManager))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @NonNull
    private String maskPhone(@Nullable String phone) {
        if (phone == null || phone.length() < 11) {
            return "138****5678";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
