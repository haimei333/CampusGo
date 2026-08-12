package com.campusgo.domain.repository;

import com.campusgo.domain.model.UserVoucher;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository {

    UserVoucher save(UserVoucher voucher);

    Optional<UserVoucher> findById(long id);

    Optional<UserVoucher> findByCode(String voucherCode);

    List<UserVoucher> findByUserId(long userId);

    List<UserVoucher> findByUserIdAndStatus(long userId, String status);

    void markUsed(long id);
}
