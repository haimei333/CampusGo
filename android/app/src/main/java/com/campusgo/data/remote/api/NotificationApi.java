package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.notification.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotificationApi {

    @GET("api/v1/notifications")
    Call<ApiResponse<List<NotificationDto>>> list();

    @GET("api/v1/notifications/unread-count")
    Call<ApiResponse<Integer>> unreadCount();

    @POST("api/v1/notifications/{id}/read")
    Call<ApiResponse<Void>> markRead(@Path("id") String id);

    @POST("api/v1/notifications/read-all")
    Call<ApiResponse<Void>> markAllRead();
}
