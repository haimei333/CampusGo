package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.wallet.TopUpRequest;
import com.campusgo.data.remote.dto.wallet.WalletResponse;
import com.campusgo.data.remote.dto.wallet.WalletTransactionDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WalletApi {

    @GET("api/v1/wallet")
    Call<ApiResponse<WalletResponse>> getWallet();

    @GET("api/v1/wallet/transactions")
    Call<ApiResponse<java.util.List<WalletTransactionDto>>> getTransactions();

    @POST("api/v1/wallet/topup")
    Call<ApiResponse<WalletResponse>> topup(@Body TopUpRequest request);
}
