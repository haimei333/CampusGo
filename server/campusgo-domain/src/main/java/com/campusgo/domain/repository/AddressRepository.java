package com.campusgo.domain.repository;

import com.campusgo.domain.model.UserAddress;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {

    List<UserAddress> listByUserId(long userId);

    Optional<UserAddress> findById(long id);

    long countByUserId(long userId);

    UserAddress save(UserAddress address);

    void delete(long id);

    void clearDefault(long userId);
}
