package com.campusgo.api.controller;

import com.campusgo.api.common.ApiResponse;
import com.campusgo.api.dto.address.AddressDto;
import com.campusgo.api.dto.address.AddressUpsertRequest;
import com.campusgo.api.security.AuthUser;
import com.campusgo.application.address.AddressService;
import com.campusgo.domain.model.UserAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Address", description = "常用地址")
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public ApiResponse<List<AddressDto>> list(@AuthenticationPrincipal AuthUser user) {
        List<AddressDto> list = addressService.list(user.userId()).stream()
                .map(AddressDto::from)
                .toList();
        return ApiResponse.ok(list);
    }

    @Operation(summary = "地址详情")
    @GetMapping("/{id}")
    public ApiResponse<AddressDto> get(@AuthenticationPrincipal AuthUser user,
                                       @PathVariable("id") long id) {
        return ApiResponse.ok(AddressDto.from(addressService.get(user.userId(), id)));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public ApiResponse<AddressDto> create(@AuthenticationPrincipal AuthUser user,
                                          @Valid @RequestBody AddressUpsertRequest request) {
        UserAddress created = addressService.create(
                user.userId(),
                request.getTitle(),
                request.getDetail(),
                request.getType(),
                request.isDefault());
        return ApiResponse.ok(AddressDto.from(created));
    }

    @Operation(summary = "更新地址")
    @PutMapping("/{id}")
    public ApiResponse<AddressDto> update(@AuthenticationPrincipal AuthUser user,
                                          @PathVariable("id") long id,
                                          @Valid @RequestBody AddressUpsertRequest request) {
        UserAddress updated = addressService.update(
                user.userId(),
                id,
                request.getTitle(),
                request.getDetail(),
                request.getType(),
                request.isDefault());
        return ApiResponse.ok(AddressDto.from(updated));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthUser user,
                                    @PathVariable("id") long id) {
        addressService.delete(user.userId(), id);
        return ApiResponse.ok(null);
    }
}
