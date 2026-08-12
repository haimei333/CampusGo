package com.campusgo.data.remote;

import androidx.annotation.NonNull;

public interface ApiCallback<T> {

    void onSuccess(@NonNull T data);

    void onError(@NonNull ApiException error);
}
