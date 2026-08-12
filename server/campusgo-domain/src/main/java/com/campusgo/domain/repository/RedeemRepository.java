package com.campusgo.domain.repository;

import com.campusgo.domain.model.RedeemRecord;

import java.util.List;

public interface RedeemRepository {

    RedeemRecord save(RedeemRecord record);

    List<RedeemRecord> findByUserId(long userId);
}