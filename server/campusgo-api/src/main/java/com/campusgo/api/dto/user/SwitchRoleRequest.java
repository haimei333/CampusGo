package com.campusgo.api.dto.user;

import com.campusgo.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchRoleRequest {

    @NotNull
    private UserRole activeRole;
}
