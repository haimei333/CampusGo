package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAddressJpaRepository extends JpaRepository<UserAddressEntity, Long> {

    List<UserAddressEntity> findByUserIdOrderByIsDefaultDescIdAsc(Long userId);

    long countByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserAddressEntity a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
