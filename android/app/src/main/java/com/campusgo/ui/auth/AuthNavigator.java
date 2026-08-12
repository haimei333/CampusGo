package com.campusgo.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;

/**
 * 登录 / 引导 / 资料编辑 / 登出
 */
public final class AuthNavigator {

    private AuthNavigator() {
    }

    @NonNull
    public static Intent guide(@NonNull Context context) {
        return new Intent(context, GuideActivity.class);
    }

    @NonNull
    public static Intent editProfile(@NonNull Context context) {
        return new Intent(context, EditProfileActivity.class);
    }

    /** 远程登出（可选）后清本地会话并跳转登录页 */
    public static void performLogout(@NonNull Activity activity, @NonNull SessionManager sessionManager) {
        Runnable finish = () -> {
            sessionManager.logout();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        };
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().authRemote().logout(new ApiCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    activity.runOnUiThread(finish);
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    activity.runOnUiThread(finish);
                }
            });
        } else {
            finish.run();
        }
    }
}
