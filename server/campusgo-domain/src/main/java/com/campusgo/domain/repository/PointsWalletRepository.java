package com.campusgo.domain.repository;

import com.campusgo.domain.model.PointsWallet;

import java.util.Optional;

public interface PointsWalletRepository {

    Optional<PointsWallet> findByUserId(long userId);

    void initWallet(long userId);

    int addPoints(long userId, int amount);

    int deductPoints(long userId, int amount);

    PointsWallet save(PointsWallet wallet);
}