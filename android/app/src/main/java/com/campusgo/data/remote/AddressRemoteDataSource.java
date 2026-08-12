package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.data.remote.api.AddressApi;
import com.campusgo.data.remote.dto.address.AddressDto;
import com.campusgo.data.remote.dto.address.AddressUpsertRequest;
import com.campusgo.domain.model.SavedAddress;

import java.util.ArrayList;
import java.util.List;

public class AddressRemoteDataSource {

    private final AddressApi addressApi;

    public AddressRemoteDataSource(@NonNull AddressApi addressApi) {
        this.addressApi = addressApi;
    }

    public void list(@NonNull ApiCallback<List<SavedAddress>> callback) {
        ApiExecutor.enqueue(addressApi.list(), new ApiCallback<List<AddressDto>>() {
            @Override
            public void onSuccess(@NonNull List<AddressDto> data) {
                callback.onSuccess(mapList(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void get(@NonNull String id, @NonNull ApiCallback<SavedAddress> callback) {
        ApiExecutor.enqueue(addressApi.get(id), new ApiCallback<AddressDto>() {
            @Override
            public void onSuccess(@NonNull AddressDto data) {
                callback.onSuccess(mapOne(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void create(@NonNull SavedAddress address, @NonNull ApiCallback<SavedAddress> callback) {
        ApiExecutor.enqueue(addressApi.create(toRequest(address)), new ApiCallback<AddressDto>() {
            @Override
            public void onSuccess(@NonNull AddressDto data) {
                callback.onSuccess(mapOne(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void update(@NonNull SavedAddress address, @NonNull ApiCallback<SavedAddress> callback) {
        ApiExecutor.enqueue(addressApi.update(address.id, toRequest(address)), new ApiCallback<AddressDto>() {
            @Override
            public void onSuccess(@NonNull AddressDto data) {
                callback.onSuccess(mapOne(data));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void delete(@NonNull String id, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(addressApi.delete(id), callback);
    }

    @NonNull
    private static AddressUpsertRequest toRequest(@NonNull SavedAddress address) {
        return new AddressUpsertRequest(
                address.title,
                address.detail,
                address.type.name(),
                address.isDefault);
    }

    @NonNull
    private static List<SavedAddress> mapList(@NonNull List<AddressDto> data) {
        List<SavedAddress> list = new ArrayList<>(data.size());
        for (AddressDto dto : data) {
            list.add(mapOne(dto));
        }
        return list;
    }

    @NonNull
    private static SavedAddress mapOne(@NonNull AddressDto dto) {
        SavedAddress.Type type;
        try {
            type = SavedAddress.Type.valueOf(dto.type != null ? dto.type : "OTHER");
        } catch (IllegalArgumentException e) {
            type = SavedAddress.Type.OTHER;
        }
        return new SavedAddress(
                dto.id != null ? dto.id : "",
                type,
                dto.title != null ? dto.title : "",
                dto.detail != null ? dto.detail : "",
                dto.isDefault);
    }
}
