package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.points.UseVoucherRequest;
import com.campusgo.data.remote.dto.points.UserVoucherDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface VoucherApi {

    @GET("api/v1/vouchers")
    Call<ApiResponse<List<UserVoucherDto>>> getVouchers(@Query("status") String status);

    @POST("api/v1/vouchers/use")
    Call<ApiResponse<UserVoucherDto>> useVoucher(@Body UseVoucherRequest request);
}
