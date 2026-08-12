package com.campusgo.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityEditProfileBinding;

/**
 * A03 编辑资料
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int MAX_NICKNAME = 20;

    private ActivityEditProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.avatarWrap.setOnClickListener(v ->
                Toast.makeText(this, R.string.edit_profile_avatar_mock, Toast.LENGTH_SHORT).show());

        binding.etNickname.setText(sessionManager.getNickname());
        updateCounter(binding.etNickname.getText());
        binding.etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCounter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.tvAvatar.setText(sessionManager.getAvatarInitial());
    }

    private void updateCounter(@Nullable CharSequence text) {
        int len = text != null ? text.length() : 0;
        binding.tvNicknameCount.setText(getString(R.string.edit_profile_nickname_count, len, MAX_NICKNAME));
    }

    private void saveProfile() {
        String nickname = binding.etNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            Toast.makeText(this, R.string.edit_profile_nickname_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnSave.setEnabled(false);
            RetrofitClient.get().userRemote().updateNickname(nickname, new ApiCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    runOnUiThread(() -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, R.string.edit_profile_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        sessionManager.setNickname(nickname);
        Toast.makeText(this, R.string.edit_profile_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
