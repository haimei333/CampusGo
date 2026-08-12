package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.PointsApi;
import com.campusgo.data.remote.dto.points.CheckInResponseDto;
import com.campusgo.data.remote.dto.points.CheckInStatusDto;
import com.campusgo.data.remote.dto.points.MallProductDto;
import com.campusgo.data.remote.dto.points.PointsBalanceDto;
import com.campusgo.data.remote.dto.points.PointsTransactionDto;
import com.campusgo.data.remote.dto.points.RedeemRecordDto;
import com.campusgo.data.remote.dto.points.RedeemRequest;
import com.campusgo.domain.model.MallProduct;
import com.campusgo.domain.model.PointsTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PointsRemoteDataSource {

    private final PointsApi pointsApi;
    private final SessionManager sessionManager;

    public PointsRemoteDataSource(@NonNull PointsApi pointsApi, @NonNull SessionManager sessionManager) {
        this.pointsApi = pointsApi;
        this.sessionManager = sessionManager;
    }

    public void loadBalance(@NonNull ApiCallback<PointsBalanceDto> callback) {
        ApiExecutor.enqueue(pointsApi.getBalance(), new ApiCallback<PointsBalanceDto>() {
            @Override
            public void onSuccess(@NonNull PointsBalanceDto data) {
                sessionManager.addPoints(data.balance - sessionManager.getPoints());
                callback.onSuccess(data);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void loadTransactions(@NonNull ApiCallback<List<PointsTransaction>> callback) {
        ApiExecutor.enqueue(pointsApi.getTransactions(), new ApiCallback<List<PointsTransactionDto>>() {
            @Override
            public void onSuccess(@NonNull List<PointsTransactionDto> data) {
                callback.onSuccess(toTransactions(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void checkIn(@NonNull ApiCallback<CheckInResponseDto> callback) {
        ApiExecutor.enqueue(pointsApi.checkIn(), new ApiCallback<CheckInResponseDto>() {
            @Override
            public void onSuccess(@NonNull CheckInResponseDto data) {
                sessionManager.addPoints(data.rewardPoints);
                sessionManager.setCheckInStreak(data.newStreak);
                sessionManager.setCheckedInToday(true);
                callback.onSuccess(data);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void loadCheckInStatus(@NonNull ApiCallback<CheckInStatusDto> callback) {
        ApiExecutor.enqueue(pointsApi.getCheckInStatus(), new ApiCallback<CheckInStatusDto>() {
            @Override
            public void onSuccess(@NonNull CheckInStatusDto data) {
                sessionManager.setCheckInStreak(data.streak);
                sessionManager.setCheckedInToday(data.checkedInToday);
                callback.onSuccess(data);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void loadProducts(@Nullable String category, @NonNull ApiCallback<List<MallProduct>> callback) {
        ApiExecutor.enqueue(pointsApi.getProducts(category != null ? category : "ALL"),
                new ApiCallback<List<MallProductDto>>() {
                    @Override
                    public void onSuccess(@NonNull List<MallProductDto> data) {
                        callback.onSuccess(toProducts(data));
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void redeem(@NonNull String productId, @Nullable String address,
            @NonNull ApiCallback<RedeemRecordDto> callback) {
        long pid;
        try {
            pid = Long.parseLong(productId);
        } catch (NumberFormatException e) {
            callback.onError(new ApiException(-1, "无效的商品ID"));
            return;
        }
        ApiExecutor.enqueue(pointsApi.redeem(new RedeemRequest(pid, address)),
                new ApiCallback<RedeemRecordDto>() {
                    @Override
                    public void onSuccess(@NonNull RedeemRecordDto data) {
                        sessionManager.deductPoints(data.pointsCost);
                        callback.onSuccess(data);
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }

    public void loadRedeems(@NonNull ApiCallback<List<RedeemRecordDto>> callback) {
        ApiExecutor.enqueue(pointsApi.getRedeems(), callback);
    }

    @NonNull
    private static List<PointsTransaction> toTransactions(@Nullable List<PointsTransactionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<PointsTransaction> result = new ArrayList<>(dtos.size());
        for (PointsTransactionDto dto : dtos) {
            if (dto == null || dto.id == null) {
                continue;
            }
            PointsTransaction.Type type = "EARN".equals(dto.type)
                    ? PointsTransaction.Type.EARN
                    : PointsTransaction.Type.SPEND;
            String title = dto.bizType != null ? dto.bizType : "";
            if (dto.remark != null && !dto.remark.isEmpty()) {
                title = title + " - " + dto.remark;
            }
            result.add(new PointsTransaction(
                    dto.id,
                    title,
                    dto.timeLabel != null ? dto.timeLabel : "",
                    dto.amount,
                    type));
        }
        return result;
    }

    @NonNull
    private static List<MallProduct> toProducts(@Nullable List<MallProductDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<MallProduct> result = new ArrayList<>(dtos.size());
        for (MallProductDto dto : dtos) {
            if (dto == null || dto.id == null) {
                continue;
            }
            MallProduct.Category category;
            try {
                category = MallProduct.Category.valueOf(dto.category != null ? dto.category : "ALL");
            } catch (IllegalArgumentException e) {
                category = MallProduct.Category.ALL;
            }
            result.add(new MallProduct(
                    dto.id,
                    dto.name != null ? dto.name : "",
                    dto.subtitle != null ? dto.subtitle : "",
                    dto.emoji != null ? dto.emoji : "",
                    dto.pointsCost,
                    category,
                    0,
                    dto.flashSale,
                    0));
        }
        return result;
    }
}