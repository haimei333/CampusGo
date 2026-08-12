package com.campusgo.domain.model;

import com.campusgo.domain.enums.AddressTag;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UserAddress {
    long id;
    long userId;
    String name;
    String detail;
    AddressTag tag;
    boolean isDefault;
    BigDecimal lng;
    BigDecimal lat;
    int useCount;
}
