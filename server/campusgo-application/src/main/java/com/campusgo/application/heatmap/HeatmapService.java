package com.campusgo.application.heatmap;

import com.campusgo.domain.model.HeatmapData;

public interface HeatmapService {

    HeatmapData getHeatmapData(String range);
}