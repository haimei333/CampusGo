package com.campusgo.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampusAuthRequest {

    @NotBlank
    private String realName;

    @NotBlank
    private String studentId;
}
