package com.campusgo.api.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinGroupRequest {

    @NotBlank
    private String address;
}
