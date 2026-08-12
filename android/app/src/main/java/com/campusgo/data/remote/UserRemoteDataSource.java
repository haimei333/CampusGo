package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.dto.auth.RefreshTokenRequest;
import com.campusgo.data.remote.dto.user.CampusAuthRequest;
import com.campusgo.data.remote.dto.user.SwitchRoleRequest;
import com.campusgo.data.remote.dto.user.UpdateNicknameRequest;
import com.campusgo.data.remote.dto.user.UserProfileDto;
import com.campusgo.data.remote.api.UserApi;
import com.campusgo.domain.model.UserRole;

public class UserRemoteDataSource {

    private final UserApi userApi;
    private final SessionManager sessionManager;

    public UserRemoteDataSource(@NonNull UserApi userApi, @NonNull SessionManager sessionManager) {
        this.userApi = userApi;
        this.sessionManager = sessionManager;
    }

    public void loadProfile(@NonNull ApiCallback<UserProfileDto> callback) {
        ApiExecutor.enqueue(userApi.me(), new ApiCallback<UserProfileDto>() {
            @Override
            public void onSuccess(@NonNull UserProfileDto data) {
                sessionManager.applyUserProfile(data);
                callback.onSuccess(data);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void updateNickname(@NonNull String nickname, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(userApi.updateNickname(new UpdateNicknameRequest(nickname)),
                profileCallback(callback));
    }

    public void switchRole(@NonNull UserRole role, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(userApi.switchRole(new SwitchRoleRequest(role)),
                profileCallback(callback));
    }

    public void submitCampusAuth(@NonNull String realName, @NonNull String studentId,
                                 @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(userApi.submitCampusAuth(new CampusAuthRequest(realName, studentId)),
                profileCallback(callback));
    }

    @NonNull
    private ApiCallback<UserProfileDto> profileCallback(@NonNull ApiCallback<Void> callback) {
        return new ApiCallback<UserProfileDto>() {
            @Override
            public void onSuccess(@NonNull UserProfileDto data) {
                sessionManager.applyUserProfile(data);
                callback.onSuccess(null);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        };
    }
}
