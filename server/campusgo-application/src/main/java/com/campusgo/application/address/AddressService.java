package com.campusgo.application.address;

import com.campusgo.domain.enums.AddressTag;
import com.campusgo.domain.model.UserAddress;

import java.util.List;

public interface AddressService {

    List<UserAddress> list(long userId);

    UserAddress get(long userId, long addressId);

    UserAddress create(long userId, String name, String detail, AddressTag tag, boolean isDefault);

    UserAddress update(long userId, long addressId, String name, String detail, AddressTag tag, boolean isDefault);

    void delete(long userId, long addressId);
}
