package com.campusgo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.databinding.ActivitySettingsBinding;
import com.campusgo.databinding.ItemSettingsSwitchBinding;
import com.campusgo.ui.auth.AuthNavigator;
import com.campusgo.ui.auth.AuthNavigator;
import com.campusgo.ui.address.AddressNavigator;

/**
 * S05 设置
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        setupNotifySwitches();
        renderCacheSize();

        binding.rowAddress.setOnClickListener(v ->
                startActivity(AddressNavigator.manage(this)));
        binding.rowSecurity.setOnClickListener(v ->
                startActivity(SettingsNavigator.security(this)));
        binding.rowResetGuide.setOnClickListener(v -> {
            sessionManager.resetGuide();
            startActivity(AuthNavigator.guide(this));
        });
        binding.rowClearCache.setOnClickListener(v -> confirmClearCache());
        binding.rowAbout.setOnClickListener(v -> showAboutDialog());
        binding.rowHelp.setOnClickListener(v ->
                startActivity(com.campusgo.ui.profile.ProfileNavigator.help(this)));
        binding.rowPrivacy.setOnClickListener(v ->
                startActivity(SettingsNavigator.privacy(this)));
        binding.rowAgreement.setOnClickListener(v ->
                startActivity(SettingsNavigator.agreement(this)));
        binding.rowLogout.setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderCacheSize();
    }

    private void setupNotifySwitches() {
        bindSwitch(binding.rowNotifyMessage, R.string.settings_notify_message,
                sessionManager.isNotifyMessageEnabled(),
                sessionManager::setNotifyMessageEnabled);
        bindSwitch(binding.rowNotifySound, R.string.settings_notify_sound,
                sessionManager.isNotifySoundEnabled(),
                sessionManager::setNotifySoundEnabled);
        bindSwitch(binding.rowNotifyVibrate, R.string.settings_notify_vibrate,
                sessionManager.isNotifyVibrateEnabled(),
                sessionManager::setNotifyVibrateEnabled);
    }

    private void bindSwitch(@NonNull ItemSettingsSwitchBinding row,
                            int labelRes,
                            boolean checked,
                            @NonNull SwitchListener listener) {
        row.tvLabel.setText(labelRes);
        row.switchToggle.setChecked(checked);
        row.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onChanged(isChecked));
    }

    private void renderCacheSize() {
        binding.tvCacheSize.setText(getString(R.string.settings_cache_size,
                sessionManager.getCacheSizeMb()));
    }

    private void confirmClearCache() {
        if (sessionManager.getCacheSizeMb() <= 0.01f) {
            Toast.makeText(this, R.string.settings_cache_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_clear_cache)
                .setMessage(R.string.settings_clear_cache_msg)
                .setPositiveButton(R.string.settings_clear_cache_confirm, (d, w) -> {
                    sessionManager.clearCache();
                    renderCacheSize();
                    Toast.makeText(this, R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about)
                .setMessage(getString(R.string.settings_about_body, getString(R.string.app_name)))
                .setPositiveButton(R.string.ok, null)
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

    private interface SwitchListener {
        void onChanged(boolean enabled);
    }
}
