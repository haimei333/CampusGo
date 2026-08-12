package com.campusgo.api.dto.address;

import com.campusgo.domain.enums.AddressTag;
import com.campusgo.domain.model.UserAddress;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDto {

    private String id;
    private AddressTag type;
    private String title;
    private String detail;

    @JsonProperty("isDefault")
    private boolean isDefault;

    public static AddressDto from(UserAddress address) {
        return AddressDto.builder()
                .id(String.valueOf(address.getId()))
                .type(address.getTag())
                .title(address.getName())
                .detail(address.getDetail())
                .isDefault(address.isDefault())
                .build();
    }
}
