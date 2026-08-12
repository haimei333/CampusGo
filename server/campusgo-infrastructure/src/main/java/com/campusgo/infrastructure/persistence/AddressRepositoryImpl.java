package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.UserAddress;
import com.campusgo.domain.repository.AddressRepository;
import com.campusgo.infrastructure.persistence.entity.UserAddressEntity;
import com.campusgo.infrastructure.persistence.jpa.UserAddressJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final UserAddressJpaRepository jpaRepository;

    @Override
    public List<UserAddress> listByUserId(long userId) {
        return jpaRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAddress> findById(long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public long countByUserId(long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public UserAddress save(UserAddress address) {
        UserAddressEntity entity;
        if (address.getId() > 0) {
            entity = jpaRepository.findById(address.getId()).orElseGet(UserAddressEntity::new);
        } else {
            entity = new UserAddressEntity();
        }
        entity.setUserId(address.getUserId());
        entity.setName(address.getName());
        entity.setDetail(address.getDetail() == null ? "" : address.getDetail());
        entity.setTag(address.getTag());
        entity.setDefault(address.isDefault());
        entity.setLng(address.getLng());
        entity.setLat(address.getLat());
        entity.setUseCount(address.getUseCount());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void clearDefault(long userId) {
        jpaRepository.clearDefaultByUserId(userId);
    }

    private UserAddress toDomain(UserAddressEntity entity) {
        return UserAddress.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .detail(entity.getDetail())
                .tag(entity.getTag())
                .isDefault(entity.isDefault())
                .lng(entity.getLng())
                .lat(entity.getLat())
                .useCount(entity.getUseCount())
                .build();
    }
}
