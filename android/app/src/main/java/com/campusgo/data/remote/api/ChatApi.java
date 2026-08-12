package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.chat.ChatMessageDto;
import com.campusgo.data.remote.dto.chat.ConversationDto;
import com.campusgo.data.remote.dto.chat.SendMessageRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    @GET("api/v1/conversations")
    Call<ApiResponse<List<ConversationDto>>> list();

    @GET("api/v1/conversations/{id}")
    Call<ApiResponse<ConversationDto>> get(@Path("id") String id);

    @GET("api/v1/conversations/by-task/{taskId}")
    Call<ApiResponse<ConversationDto>> byTask(@Path("taskId") String taskId);

    @GET("api/v1/conversations/{id}/messages")
    Call<ApiResponse<List<ChatMessageDto>>> messages(@Path("id") String id,
                                                     @Query("beforeId") Long beforeId,
                                                     @Query("limit") int limit);

    @POST("api/v1/conversations/{id}/messages")
    Call<ApiResponse<ChatMessageDto>> send(@Path("id") String id, @Body SendMessageRequest request);

    @POST("api/v1/conversations/{id}/read")
    Call<ApiResponse<Void>> markRead(@Path("id") String id);
}
