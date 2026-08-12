package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.api.HeatmapApi;
import com.campusgo.data.remote.dto.heatmap.HeatmapDataDto;

public class HeatmapRemoteDataSource {

    private final HeatmapApi heatmapApi;

    public HeatmapRemoteDataSource(@NonNull HeatmapApi heatmapApi) {
        this.heatmapApi = heatmapApi;
    }

    public void loadData(@Nullable String range, @NonNull ApiCallback<HeatmapDataDto> callback) {
        ApiExecutor.enqueue(heatmapApi.getData(range != null ? range : "1h"), callback);
    }
}