package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.points.CheckInResponseDto;
import com.campusgo.data.remote.dto.points.CheckInStatusDto;
import com.campusgo.data.remote.dto.points.MallProductDto;
import com.campusgo.data.remote.dto.points.PointsBalanceDto;
import com.campusgo.data.remote.dto.points.PointsTransactionDto;
import com.campusgo.data.remote.dto.points.RedeemRecordDto;
import com.campusgo.data.remote.dto.points.RedeemRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PointsApi {

    @GET("api/v1/points")
    Call<ApiResponse<PointsBalanceDto>> getBalance();

    @GET("api/v1/points/transactions")
    Call<ApiResponse<List<PointsTransactionDto>>> getTransactions();

    @POST("api/v1/points/check-in")
    Call<ApiResponse<CheckInResponseDto>> checkIn();

    @GET("api/v1/points/check-in/status")
    Call<ApiResponse<CheckInStatusDto>> getCheckInStatus();

    @GET("api/v1/points/products")
    Call<ApiResponse<List<MallProductDto>>> getProducts(@Query("category") String category);

    @POST("api/v1/points/redeem")
    Call<ApiResponse<RedeemRecordDto>> redeem(@Body RedeemRequest request);

    @GET("api/v1/points/redeems")
    Call<ApiResponse<List<RedeemRecordDto>>> getRedeems();
}