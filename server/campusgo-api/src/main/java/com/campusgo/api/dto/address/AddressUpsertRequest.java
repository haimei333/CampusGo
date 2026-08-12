package com.campusgo.api.dto.address;

import com.campusgo.domain.enums.AddressTag;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressUpsertRequest {

    @NotBlank(message = "请填写地址名称")
    @Size(max = 40, message = "地址名称不能超过 40 字")
    private String title;

    @Size(max = 256, message = "详细地址不能超过 256 字")
    private String detail = "";

    @NotNull(message = "请选择地址类型")
    private AddressTag type;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
