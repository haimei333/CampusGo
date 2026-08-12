package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.UserVoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherJpaRepository extends JpaRepository<UserVoucherEntity, Long> {

    Optional<UserVoucherEntity> findByVoucherCode(String voucherCode);

    List<UserVoucherEntity> findByUserIdOrderByCreatedAtDesc(long userId);

    List<UserVoucherEntity> findByUserIdAndStatusOrderByCreatedAtDesc(long userId, String status);

    @Modifying
    @Query("UPDATE UserVoucherEntity v SET v.status = 'USED', v.usedAt = CURRENT_TIMESTAMP WHERE v.id = :id AND v.status = 'UNUSED'")
    int markUsed(@Param("id") long id);
}
