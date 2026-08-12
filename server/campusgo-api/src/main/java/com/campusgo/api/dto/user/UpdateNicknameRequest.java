package com.campusgo.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateNicknameRequest {

    @NotBlank
    @Size(min = 1, max = 32)
    private String nickname;
}
