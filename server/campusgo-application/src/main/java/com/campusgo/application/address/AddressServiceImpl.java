package com.campusgo.application.address;

import com.campusgo.domain.enums.AddressTag;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.UserAddress;
import com.campusgo.domain.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final int MAX_PER_USER = 30;
    private static final BigDecimal DEFAULT_LNG = new BigDecimal("116.310003");
    private static final BigDecimal DEFAULT_LAT = new BigDecimal("39.992801");

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddress> list(long userId) {
        return addressRepository.listByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddress get(long userId, long addressId) {
        return requireOwned(userId, addressId);
    }

    @Override
    @Transactional
    public UserAddress create(long userId, String name, String detail, AddressTag tag, boolean isDefault) {
        String trimmedName = requireName(name);
        String trimmedDetail = detail == null ? "" : detail.trim();
        AddressTag resolvedTag = tag == null ? AddressTag.OTHER : tag;

        if (addressRepository.countByUserId(userId) >= MAX_PER_USER) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "地址数量已达上限（最多 " + MAX_PER_USER + " 条）");
        }

        if (isDefault) {
            addressRepository.clearDefault(userId);
        } else if (addressRepository.countByUserId(userId) == 0) {
            isDefault = true;
        }

        UserAddress created = UserAddress.builder()
                .userId(userId)
                .name(trimmedName)
                .detail(trimmedDetail)
                .tag(resolvedTag)
                .isDefault(isDefault)
                .lng(DEFAULT_LNG)
                .lat(DEFAULT_LAT)
                .useCount(0)
                .build();
        return addressRepository.save(created);
    }

    @Override
    @Transactional
    public UserAddress update(long userId, long addressId, String name, String detail, AddressTag tag, boolean isDefault) {
        UserAddress existing = requireOwned(userId, addressId);
        String trimmedName = requireName(name);
        String trimmedDetail = detail == null ? "" : detail.trim();
        AddressTag resolvedTag = tag == null ? existing.getTag() : tag;

        if (isDefault) {
            addressRepository.clearDefault(userId);
        }

        UserAddress updated = UserAddress.builder()
                .id(existing.getId())
                .userId(userId)
                .name(trimmedName)
                .detail(trimmedDetail)
                .tag(resolvedTag)
                .isDefault(isDefault)
                .lng(existing.getLng())
                .lat(existing.getLat())
                .useCount(existing.getUseCount())
                .build();
        return addressRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(long userId, long addressId) {
        requireOwned(userId, addressId);
        addressRepository.delete(addressId);
    }

    private UserAddress requireOwned(long userId, long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "地址不存在"));
        if (address.getUserId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权操作该地址");
        }
        return address;
    }

    private static String requireName(String name) {
        if (!StringUtils.hasText(name)) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "请填写地址名称");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 40) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "地址名称不能超过 40 字");
        }
        return trimmed;
    }
}
