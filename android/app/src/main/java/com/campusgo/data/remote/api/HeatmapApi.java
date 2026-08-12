package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.heatmap.HeatmapDataDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HeatmapApi {

    @GET("api/v1/heatmap/data")
    Call<ApiResponse<HeatmapDataDto>> getData(@Query("range") String range);
}