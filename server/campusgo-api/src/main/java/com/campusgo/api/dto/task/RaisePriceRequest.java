package com.campusgo.api.dto.task;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RaisePriceRequest {

    @Min(1)
    private int addCent;
}
