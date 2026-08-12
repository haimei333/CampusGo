package com.campusgo.data.remote.api;

import com.campusgo.data.remote.ApiResponse;
import com.campusgo.data.remote.dto.address.AddressDto;
import com.campusgo.data.remote.dto.address.AddressUpsertRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApi {

    @GET("api/v1/addresses")
    Call<ApiResponse<List<AddressDto>>> list();

    @GET("api/v1/addresses/{id}")
    Call<ApiResponse<AddressDto>> get(@Path("id") String id);

    @POST("api/v1/addresses")
    Call<ApiResponse<AddressDto>> create(@Body AddressUpsertRequest request);

    @PUT("api/v1/addresses/{id}")
    Call<ApiResponse<AddressDto>> update(@Path("id") String id, @Body AddressUpsertRequest request);

    @DELETE("api/v1/addresses/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}
