package com.campusgo.domain.repository;

import com.campusgo.domain.model.PointsTransaction;

import java.util.List;

public interface PointsTransactionRepository {

    PointsTransaction append(long userId, String type, int amount, int balanceAfter,
                             String bizType, String bizId, String remark);

    List<PointsTransaction> listByUser(long userId, int limit);
}