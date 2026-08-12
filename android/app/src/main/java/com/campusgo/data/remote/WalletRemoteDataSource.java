package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.WalletApi;
import com.campusgo.data.remote.dto.wallet.WalletResponse;
import com.campusgo.data.remote.dto.wallet.WalletTransactionDto;
import com.campusgo.data.remote.mapper.GroupDtoMapper;
import com.campusgo.domain.model.WalletTransaction;

import java.util.List;

public class WalletRemoteDataSource {

    private final WalletApi walletApi;
    private final SessionManager sessionManager;

    public WalletRemoteDataSource(@NonNull WalletApi walletApi, @NonNull SessionManager sessionManager) {
        this.walletApi = walletApi;
        this.sessionManager = sessionManager;
    }

    public void loadWallet(@NonNull ApiCallback<WalletResponse> callback) {
        ApiExecutor.enqueue(walletApi.getWallet(), new ApiCallback<WalletResponse>() {
            @Override
            public void onSuccess(@NonNull WalletResponse data) {
                sessionManager.applyWallet(data);
                callback.onSuccess(data);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void loadTransactions(@NonNull ApiCallback<List<WalletTransaction>> callback) {
        ApiExecutor.enqueue(walletApi.getTransactions(), new ApiCallback<List<WalletTransactionDto>>() {
            @Override
            public void onSuccess(@NonNull List<WalletTransactionDto> data) {
                callback.onSuccess(GroupDtoMapper.toTransactions(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void topup(double amountYuan, @NonNull ApiCallback<WalletResponse> callback) {
        ApiExecutor.enqueue(walletApi.topup(new com.campusgo.data.remote.dto.wallet.TopUpRequest(amountYuan)),
                new ApiCallback<WalletResponse>() {
                    @Override
                    public void onSuccess(@NonNull WalletResponse data) {
                        sessionManager.applyWallet(data);
                        callback.onSuccess(data);
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        callback.onError(error);
                    }
                });
    }
}
