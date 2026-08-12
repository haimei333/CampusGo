package com.campusgo.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
public class UserVoucher {

    public static final String STATUS_UNUSED = "UNUSED";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    Long id;
    long userId;
    long productId;
    String productName;
    String voucherCode;
    String status;
    Instant expireAt;
    Instant createdAt;
    Instant usedAt;

    public boolean isUnused() {
        return STATUS_UNUSED.equals(status);
    }
}
