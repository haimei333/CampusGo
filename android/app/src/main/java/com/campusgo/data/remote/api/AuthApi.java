package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.auth.LoginRequest;
import com.campusgo.data.remote.dto.auth.LoginResponse;
import com.campusgo.data.remote.dto.auth.RefreshTokenRequest;
import com.campusgo.data.remote.dto.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/v1/auth/register")
    Call<ApiResponse<LoginResponse>> register(@Body RegisterRequest request);

    @POST("api/v1/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/refresh")
    Call<ApiResponse<LoginResponse>> refresh(@Body RefreshTokenRequest request);

    @POST("api/v1/auth/logout")
    Call<ApiResponse<Void>> logout(@Body RefreshTokenRequest request);
}
