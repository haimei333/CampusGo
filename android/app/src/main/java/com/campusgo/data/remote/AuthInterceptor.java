package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自动附加 JWT：Authorization: Bearer &lt;token&gt;
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(@NonNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder()
                .header("Accept", "application/json");

        if (FeatureFlags.DEBUG) {
            builder.header("X-Client", "CampusGo-Android-Debug");
        }

        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}
