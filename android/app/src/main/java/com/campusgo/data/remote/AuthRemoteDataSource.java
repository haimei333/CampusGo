package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.AuthApi;
import com.campusgo.data.remote.api.PointsApi;
import com.campusgo.data.remote.dto.auth.LoginRequest;
import com.campusgo.data.remote.dto.auth.LoginResponse;
import com.campusgo.data.remote.dto.auth.RefreshTokenRequest;
import com.campusgo.data.remote.dto.auth.RegisterRequest;
import com.campusgo.data.remote.dto.points.PointsBalanceDto;

/**
 * 认证相关远程调用，成功后写入 {@link SessionManager}。
 */
public class AuthRemoteDataSource {

    private final AuthApi authApi;
    private final PointsApi pointsApi;
    private final SessionManager sessionManager;

    public AuthRemoteDataSource(@NonNull AuthApi authApi, @NonNull PointsApi pointsApi,
            @NonNull SessionManager sessionManager) {
        this.authApi = authApi;
        this.pointsApi = pointsApi;
        this.sessionManager = sessionManager;
    }

    public void register(@NonNull String phone, @NonNull String password,
            @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(authApi.register(new RegisterRequest(phone, password)),
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(@NonNull LoginResponse data) {
                        sessionManager.applyRemoteLogin(phone, data);
                        // Load points balance after login
                        loadPointsBalance(callback);
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void login(@NonNull String phone, @NonNull String password, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(authApi.login(new LoginRequest(phone, password)), new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(@NonNull LoginResponse data) {
                sessionManager.applyRemoteLogin(phone, data);
                // Load points balance after login
                loadPointsBalance(callback);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    private void loadPointsBalance(@NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(pointsApi.getBalance(), new ApiCallback<PointsBalanceDto>() {
            @Override
            public void onSuccess(@NonNull PointsBalanceDto data) {
                sessionManager.setPoints(data.balance);
                callback.onSuccess(null);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                // Points loading failed, but login succeeded
                callback.onSuccess(null);
            }
        });
    }

    public void refresh(@NonNull ApiCallback<Void> callback) {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            callback.onError(new ApiException(-1, "无 refreshToken"));
            return;
        }
        ApiExecutor.enqueue(authApi.refresh(new RefreshTokenRequest(refreshToken)),
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(@NonNull LoginResponse data) {
                        String phone = sessionManager.getPhone();
                        sessionManager.applyRemoteLogin(phone != null ? phone : "", data);
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void logout(@NonNull ApiCallback<Void> callback) {
        String refreshToken = sessionManager.getRefreshToken();
        RefreshTokenRequest request = new RefreshTokenRequest(
                refreshToken != null ? refreshToken : "");
        ApiExecutor.enqueue(authApi.logout(request), callback);
    }
}
