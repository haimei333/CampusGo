package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.user.CampusAuthRequest;
import com.campusgo.data.remote.dto.user.SwitchRoleRequest;
import com.campusgo.data.remote.dto.user.UpdateNicknameRequest;
import com.campusgo.data.remote.dto.user.UserProfileDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UserApi {

    @GET("api/v1/users/me")
    Call<ApiResponse<UserProfileDto>> me();

    @PATCH("api/v1/users/me")
    Call<ApiResponse<UserProfileDto>> updateNickname(@Body UpdateNicknameRequest request);

    @PUT("api/v1/users/me/role")
    Call<ApiResponse<UserProfileDto>> switchRole(@Body SwitchRoleRequest request);

    @POST("api/v1/users/me/campus-auth")
    Call<ApiResponse<UserProfileDto>> submitCampusAuth(@Body CampusAuthRequest request);
}
