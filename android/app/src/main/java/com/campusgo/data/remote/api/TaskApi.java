package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.common.PageResponse;
import com.campusgo.data.remote.dto.task.CancelTaskRequest;
import com.campusgo.data.remote.dto.task.DeliveryPhotoRequest;
import com.campusgo.data.remote.dto.task.GroupOrderDetailDto;
import com.campusgo.data.remote.dto.task.JoinGroupRequest;
import com.campusgo.data.remote.dto.task.PublishTaskRequest;
import com.campusgo.data.remote.dto.task.PublishTaskResponse;
import com.campusgo.data.remote.dto.task.RaisePriceRequest;
import com.campusgo.data.remote.dto.task.SaveDraftRequest;
import com.campusgo.data.remote.dto.task.TaskDraftDto;
import com.campusgo.data.remote.dto.task.TaskDetailDto;
import com.campusgo.data.remote.dto.task.TaskListItemDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface TaskApi {

    @GET("api/v1/tasks/drafts")
    Call<ApiResponse<List<TaskDraftDto>>> listDrafts();

    @POST("api/v1/tasks/drafts")
    Call<ApiResponse<TaskDraftDto>> createDraft(@Body SaveDraftRequest request);

    @PUT("api/v1/tasks/drafts/{id}")
    Call<ApiResponse<TaskDraftDto>> updateDraft(@Path("id") String id, @Body SaveDraftRequest request);

    @DELETE("api/v1/tasks/drafts/{id}")
    Call<ApiResponse<Void>> deleteDraft(@Path("id") String id);

    @GET("api/v1/tasks/hall")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> hall(
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/pool")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> pool(
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/mine/published")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> minePublished(
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/mine/accepted")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> mineAccepted(
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/mine/reservations")
    Call<ApiResponse<PageResponse<TaskListItemDto>>> mineReservations(
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/v1/tasks/{id}")
    Call<ApiResponse<TaskDetailDto>> detail(@Path("id") String id);

    @POST("api/v1/tasks/publish")
    Call<ApiResponse<PublishTaskResponse>> publish(@Body PublishTaskRequest request);

    @POST("api/v1/tasks/{id}/grab")
    Call<ApiResponse<TaskDetailDto>> grab(@Path("id") String id);

    @POST("api/v1/tasks/{id}/reserve/hold")
    Call<ApiResponse<TaskDetailDto>> holdReserve(@Path("id") String id);

    @POST("api/v1/tasks/{id}/reserve/release")
    Call<ApiResponse<TaskDetailDto>> releaseReserve(@Path("id") String id);

    @POST("api/v1/tasks/{id}/reserve/confirm")
    Call<ApiResponse<TaskDetailDto>> confirmReserve(@Path("id") String id);

    @POST("api/v1/tasks/{id}/deliver/start")
    Call<ApiResponse<TaskDetailDto>> startDeliver(@Path("id") String id);

    @POST("api/v1/tasks/{id}/deliver/photo")
    Call<ApiResponse<TaskDetailDto>> uploadPhoto(@Path("id") String id, @Body DeliveryPhotoRequest request);

    @POST("api/v1/tasks/{id}/confirm")
    Call<ApiResponse<TaskDetailDto>> confirm(@Path("id") String id);

    @POST("api/v1/tasks/{id}/raise-price")
    Call<ApiResponse<TaskDetailDto>> raisePrice(@Path("id") String id, @Body RaisePriceRequest request);

    @POST("api/v1/tasks/{id}/emergency")
    Call<ApiResponse<TaskDetailDto>> emergency(@Path("id") String id);

    @POST("api/v1/tasks/{id}/cancel")
    Call<ApiResponse<TaskDetailDto>> cancel(@Path("id") String id, @Body CancelTaskRequest request);

    @GET("api/v1/tasks/{id}/group")
    Call<ApiResponse<GroupOrderDetailDto>> groupDetail(@Path("id") String id);

    @POST("api/v1/tasks/{id}/group/join")
    Call<ApiResponse<GroupOrderDetailDto>> joinGroup(@Path("id") String id, @Body JoinGroupRequest request);

    @POST("api/v1/tasks/{id}/group/leave")
    Call<ApiResponse<GroupOrderDetailDto>> leaveGroup(@Path("id") String id);

    @POST("api/v1/tasks/{id}/reviews")
    Call<ApiResponse<Void>> submitReview(@Path("id") String id,
                                         @Body com.campusgo.data.remote.dto.review.SubmitReviewRequest request);
}
