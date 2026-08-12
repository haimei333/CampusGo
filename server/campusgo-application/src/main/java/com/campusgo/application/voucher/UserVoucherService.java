package com.campusgo.application.voucher;

import com.campusgo.domain.model.UserVoucher;

import java.util.List;

public interface UserVoucherService {

    UserVoucher createVoucher(long userId, long productId, String productName);

    List<UserVoucher> getUserVouchers(long userId);

    List<UserVoucher> getUserUnusedVouchers(long userId);

    UserVoucher useVoucher(long userId, String voucherCode);
}
