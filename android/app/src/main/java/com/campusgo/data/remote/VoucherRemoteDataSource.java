package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.api.VoucherApi;
import com.campusgo.data.remote.dto.points.UseVoucherRequest;
import com.campusgo.data.remote.dto.points.UserVoucherDto;

import java.util.List;

public class VoucherRemoteDataSource {

    private final VoucherApi voucherApi;

    public VoucherRemoteDataSource(@NonNull VoucherApi voucherApi) {
        this.voucherApi = voucherApi;
    }

    public void loadVouchers(@Nullable String status, @NonNull ApiCallback<List<UserVoucherDto>> callback) {
        ApiExecutor.enqueue(voucherApi.getVouchers(status), callback);
    }

    public void useVoucher(@NonNull String voucherCode, @NonNull ApiCallback<UserVoucherDto> callback) {
        ApiExecutor.enqueue(voucherApi.useVoucher(new UseVoucherRequest(voucherCode)), callback);
    }
}
