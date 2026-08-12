package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "签到状态")
public class CheckInStatusResponse {

    int streak;
    boolean checkedInToday;
    List<String> monthDates;
}