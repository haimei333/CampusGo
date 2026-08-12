package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.dashboard.DashboardStatsDto;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DashboardApi {

    @GET("api/v1/dashboard/stats")
    Call<ApiResponse<DashboardStatsDto>> getStats();
}