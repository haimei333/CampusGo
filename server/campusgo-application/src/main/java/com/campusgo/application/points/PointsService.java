package com.campusgo.application.points;

import com.campusgo.domain.enums.MallProductCategory;
import com.campusgo.domain.model.MallProduct;
import com.campusgo.domain.model.PointsTransaction;
import com.campusgo.domain.model.RedeemRecord;

import java.time.LocalDate;
import java.util.List;

public interface PointsService {

    int getBalance(long userId);

    List<PointsTransaction> listTransactions(long userId);

    /** 签到，返回本次获得积分 */
    int checkIn(long userId);

    /** 返回 streak + 今日是否已签 + 当月签到记录 */
    CheckInStatus getCheckInStatus(long userId);

    /** 积分商城商品列表 */
    List<MallProduct> listProducts(MallProductCategory category);

    /** 兑换商品 */
    RedeemRecord redeem(long userId, long productId, String address);

    /** 兑换记录 */
    List<RedeemRecord> listRedeems(long userId);

    /** 增加积分并记录流水 */
    int earnPoints(long userId, int amount, String bizType, String bizId, String remark);

    /** 扣减积分并记录流水 */
    int spendPoints(long userId, int amount, String bizType, String bizId, String remark);

    record CheckInStatus(int streak, boolean checkedInToday, List<LocalDate> monthDates) {
    }
}