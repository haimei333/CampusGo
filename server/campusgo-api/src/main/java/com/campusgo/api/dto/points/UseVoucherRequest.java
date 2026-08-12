package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "使用券请求")
public class UseVoucherRequest {

    @NotBlank
    @Schema(description = "券码")
    private String voucherCode;
}
