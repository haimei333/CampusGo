package com.campusgo.application.voucher;

import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.UserVoucher;
import com.campusgo.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService {

    private final UserVoucherRepository voucherRepository;

    @Override
    @Transactional
    public UserVoucher createVoucher(long userId, long productId, String productName) {
        String voucherCode = generateVoucherCode();
        Instant expireAt = Instant.now().plus(30, ChronoUnit.DAYS);

        UserVoucher voucher = UserVoucher.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .voucherCode(voucherCode)
                .status(UserVoucher.STATUS_UNUSED)
                .expireAt(expireAt)
                .build();

        return voucherRepository.save(voucher);
    }

    @Override
    public List<UserVoucher> getUserVouchers(long userId) {
        return voucherRepository.findByUserId(userId);
    }

    @Override
    public List<UserVoucher> getUserUnusedVouchers(long userId) {
        return voucherRepository.findByUserIdAndStatus(userId, UserVoucher.STATUS_UNUSED);
    }

    @Override
    @Transactional
    public UserVoucher useVoucher(long userId, String voucherCode) {
        UserVoucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "券不存在"));

        if (voucher.getUserId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权使用该券");
        }

        if (!voucher.isUnused()) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "券已使用或已过期");
        }

        if (voucher.getExpireAt().isBefore(Instant.now())) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "券已过期");
        }

        voucherRepository.markUsed(voucher.getId());
        return voucherRepository.findById(voucher.getId()).orElseThrow();
    }

    private String generateVoucherCode() {
        return "CG" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
