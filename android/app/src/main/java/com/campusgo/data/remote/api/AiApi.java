package com.campusgo.data.remote.api;

import com.campusgo.data.remote.dto.ai.ChatRequest;
import com.campusgo.data.remote.dto.ai.ChatResponse;
import com.campusgo.data.remote.dto.ai.HistoryResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AiApi {

    @POST("api/ai/chat")
    Call<ChatResponse> chat(@Body ChatRequest request);

    @GET("api/ai/history")
    Call<HistoryResponse> getHistory(@Query("sessionId") String sessionId);

    @DELETE("api/ai/session/{sessionId}")
    Call<Void> clearSession(@Path("sessionId") String sessionId);
}
