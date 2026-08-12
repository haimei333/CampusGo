package com.campusgo.data.remote;

import android.util.Log;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.AiApi;
import com.campusgo.data.remote.dto.ai.ChatRequest;
import com.campusgo.data.remote.dto.ai.ChatResponse;
import com.campusgo.data.remote.dto.ai.HistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRemoteDataSource {
    private static final String TAG = "AiRemoteDataSource";
    private final AiApi aiApi;
    private final SessionManager sessionManager;

    public AiRemoteDataSource(AiApi aiApi, SessionManager sessionManager) {
        this.aiApi = aiApi;
        this.sessionManager = sessionManager;
    }

    public interface ChatCallback {
        void onSuccess(String sessionId, String reply);
        void onError(String error);
    }

    public interface HistoryCallback {
        void onSuccess(List<HistoryResponse.Message> messages);
        void onError(String error);
    }

    public interface SessionListCallback {
        void onSuccess(List<String> sessions);
        void onError(String error);
    }

    public interface ClearCallback {
        void onSuccess();
        void onError(String error);
    }

    public void chat(String sessionId, String message, ChatCallback callback) {
        String token = sessionManager.getToken();
        if (token == null) {
            callback.onError("请先登录");
            return;
        }

        ChatRequest request = new ChatRequest(sessionId, message);
        aiApi.chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getSessionId(), response.body().getReply());
                } else {
                    callback.onError("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e(TAG, "Chat failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    public void getHistory(String sessionId, HistoryCallback callback) {
        String token = sessionManager.getToken();
        if (token == null) {
            callback.onError("请先登录");
            return;
        }

        aiApi.getHistory(sessionId).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessages());
                } else {
                    callback.onError("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                Log.e(TAG, "Get history failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    public void clearSession(String sessionId, ClearCallback callback) {
        String token = sessionManager.getToken();
        if (token == null) {
            callback.onError("请先登录");
            return;
        }

        aiApi.clearSession(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Clear session failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    public void getSessionList(SessionListCallback callback) {
        String token = sessionManager.getToken();
        if (token == null) {
            callback.onError("请先登录");
            return;
        }

        aiApi.getHistory(null).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getSessions());
                } else {
                    callback.onError("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                Log.e(TAG, "Get session list failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
}
