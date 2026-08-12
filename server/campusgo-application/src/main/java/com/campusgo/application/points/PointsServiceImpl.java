package com.campusgo.application.points;

import com.campusgo.domain.enums.MallProductCategory;
import com.campusgo.domain.enums.PointsBizType;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.CheckInRecord;
import com.campusgo.domain.model.MallProduct;
import com.campusgo.domain.model.PointsWallet;
import com.campusgo.domain.model.RedeemRecord;
import com.campusgo.domain.repository.CheckInRepository;
import com.campusgo.domain.repository.MallProductRepository;
import com.campusgo.domain.repository.PointsTransactionRepository;
import com.campusgo.domain.repository.PointsWalletRepository;
import com.campusgo.domain.repository.RedeemRepository;
import com.campusgo.application.voucher.UserVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private static final int CHECKIN_BASE_POINTS = 5;
    private static final int CHECKIN_STREAK_BONUS = 20;
    private static final int STREAK_CYCLE = 7;

    private final PointsWalletRepository walletRepository;
    private final PointsTransactionRepository transactionRepository;
    private final CheckInRepository checkInRepository;
    private final MallProductRepository mallProductRepository;
    private final RedeemRepository redeemRepository;
    private final UserVoucherService userVoucherService;

    @Override
    public int getBalance(long userId) {
        return walletRepository.findByUserId(userId)
                .map(PointsWallet::getBalance)
                .orElse(0);
    }

    @Override
    public List<com.campusgo.domain.model.PointsTransaction> listTransactions(long userId) {
        return transactionRepository.listByUser(userId, 50);
    }

    @Override
    @Transactional
    public int checkIn(long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        if (checkInRepository.findByUserAndDate(userId, today).isPresent()) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "今日已签到");
        }

        // 获取上次签到日期，计算连续天数
        int streak = 1;
        CheckInRecord lastRecord = checkInRepository.findLatestBefore(userId, today).orElse(null);
        if (lastRecord != null) {
            LocalDate lastDate = lastRecord.getCheckInDate();
            if (lastDate.equals(today.minusDays(1))) {
                streak = lastRecord.getStreak() + 1;
            }
        }

        // 计算本次获得积分
        int rewardPoints = CHECKIN_BASE_POINTS;
        if (streak % STREAK_CYCLE == 0) {
            rewardPoints += CHECKIN_STREAK_BONUS;
        }

        // 创建签到记录
        CheckInRecord record = CheckInRecord.builder()
                .userId(userId)
                .checkInDate(today)
                .streak(streak)
                .rewardPoints(rewardPoints)
                .build();
        checkInRepository.save(record);

        // 确保钱包存在，不存在则初始化
        if (walletRepository.findByUserId(userId).isEmpty()) {
            walletRepository.initWallet(userId);
        }

        // 增加积分
        int newBalance = walletRepository.addPoints(userId, rewardPoints);

        // 记录流水
        transactionRepository.append(userId, "IN", rewardPoints, newBalance,
                PointsBizType.CHECKIN.name(), String.valueOf(record.getId()), "签到奖励");

        return rewardPoints;
    }

    @Override
    public CheckInStatus getCheckInStatus(long userId) {
        LocalDate today = LocalDate.now();

        // 今日是否已签到
        boolean checkedInToday = checkInRepository.findByUserAndDate(userId, today).isPresent();

        // 获取连续天数
        int streak = 0;
        CheckInRecord lastRecord = checkInRepository.findLatestBefore(userId, today).orElse(null);
        if (lastRecord != null) {
            if (lastRecord.getCheckInDate().equals(today) || lastRecord.getCheckInDate().equals(today.minusDays(1))) {
                streak = lastRecord.getStreak();
            }
        }

        // 当月签到记录
        List<CheckInRecord> monthRecords = checkInRepository.findMonthRecords(userId, today.getYear(),
                today.getMonthValue());
        List<LocalDate> monthDates = monthRecords.stream()
                .map(CheckInRecord::getCheckInDate)
                .sorted()
                .toList();

        return new CheckInStatus(streak, checkedInToday, monthDates);
    }

    @Override
    public List<MallProduct> listProducts(MallProductCategory category) {
        List<MallProduct> all = mallProductRepository.findAllEnabled();
        if (category == null || category == MallProductCategory.ALL) {
            return all;
        }
        if (category == MallProductCategory.FLASH) {
            return all.stream()
                    .filter(MallProduct::isFlashSale)
                    .toList();
        }
        return all.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().equals(category.name()))
                .toList();
    }

    @Override
    @Transactional
    public RedeemRecord redeem(long userId, long productId, String address) {
        // 查询商品
        MallProduct product = mallProductRepository.findById(productId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "商品不存在"));

        // 检查库存
        if (product.getStock() <= 0) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "商品库存不足");
        }

        // 检查积分并扣减
        int newBalance = walletRepository.deductPoints(userId, product.getPointsCost());

        // 减库存（乐观锁）
        boolean stockOk = mallProductRepository.deductStock(productId, 1);
        if (!stockOk) {
            // 扣库存失败，回滚积分
            walletRepository.addPoints(userId, product.getPointsCost());
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "商品库存不足");
        }

        // 创建兑换记录
        RedeemRecord redeemRecord = RedeemRecord.builder()
                .userId(userId)
                .productId(productId)
                .productName(product.getName())
                .pointsCost(product.getPointsCost())
                .address(address)
                .status("PENDING")
                .build();
        redeemRecord = redeemRepository.save(redeemRecord);

        // 记录流水
        transactionRepository.append(userId, "OUT", product.getPointsCost(), newBalance,
                PointsBizType.REDEEM.name(), String.valueOf(redeemRecord.getId()),
                "兑换商品：" + product.getName());

        // 创建用户券
        try {
            userVoucherService.createVoucher(userId, productId, product.getName());
        } catch (Exception e) {
            // 券创建失败不影响兑换流程，记录日志即可
            log.warn("创建用户券失败: userId={}, productId={}", userId, productId, e);
        }

        return redeemRecord;
    }

    @Override
    public List<RedeemRecord> listRedeems(long userId) {
        return redeemRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public int earnPoints(long userId, int amount, String bizType, String bizId, String remark) {
        int newBalance = walletRepository.addPoints(userId, amount);
        transactionRepository.append(userId, "IN", amount, newBalance, bizType, bizId, remark);
        return newBalance;
    }

    @Override
    @Transactional
    public int spendPoints(long userId, int amount, String bizType, String bizId, String remark) {
        int newBalance = walletRepository.deductPoints(userId, amount);
        transactionRepository.append(userId, "OUT", amount, newBalance, bizType, bizId, remark);
        return newBalance;
    }
}