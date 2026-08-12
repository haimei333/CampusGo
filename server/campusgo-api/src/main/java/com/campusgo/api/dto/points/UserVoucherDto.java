package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "用户券")
public class UserVoucherDto {

    String id;
    String productName;
    String voucherCode;
    String status;
    String expireAt;
    String createdAt;
    String usedAt;
}
