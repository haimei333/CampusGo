package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 统一处理 Retrofit 回调与 {@link ApiResponse} 业务码。
 */
public final class ApiExecutor {

    private ApiExecutor() {
    }

    public static <T> void enqueue(@NonNull Call<ApiResponse<T>> call, @NonNull ApiCallback<T> callback) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> call,
                                   @NonNull Response<ApiResponse<T>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new ApiException(response.code(), "HTTP " + response.code()));
                    return;
                }
                ApiResponse<T> body = response.body();
                if (body.isSuccess()) {
                    if (body.data != null) {
                        callback.onSuccess(body.data);
                    } else {
                        callback.onSuccess(null);
                    }
                } else {
                    callback.onError(new ApiException(body.code,
                            body.message != null ? body.message : "请求失败"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> call, @NonNull Throwable t) {
                String message = t instanceof IOException ? "网络连接失败" : t.getMessage();
                callback.onError(new ApiException(-1, message != null ? message : "未知错误"));
            }
        });
    }
}
