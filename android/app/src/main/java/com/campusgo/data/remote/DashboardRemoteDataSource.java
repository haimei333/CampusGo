package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.data.remote.api.DashboardApi;
import com.campusgo.data.remote.dto.dashboard.DashboardStatsDto;

public class DashboardRemoteDataSource {

    private final DashboardApi dashboardApi;

    public DashboardRemoteDataSource(@NonNull DashboardApi dashboardApi) {
        this.dashboardApi = dashboardApi;
    }

    public void loadStats(@NonNull ApiCallback<DashboardStatsDto> callback) {
        ApiExecutor.enqueue(dashboardApi.getStats(), callback);
    }
}