package com.campusgo.ui.address;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * T04 地址管理 / 选择跳转
 */
public final class AddressNavigator {

    public static final String EXTRA_SELECT_MODE = "select_mode";
    public static final String EXTRA_PICKUP = "pickup";
    public static final String EXTRA_EDIT_ID = "edit_id";
    public static final String EXTRA_RESULT_DISPLAY = "result_display";

    private AddressNavigator() {
    }

    @NonNull
    public static Intent manage(@NonNull Context context) {
        return new Intent(context, AddressActivity.class);
    }

    @NonNull
    public static Intent pick(@NonNull Context context, boolean pickup) {
        return manage(context)
                .putExtra(EXTRA_SELECT_MODE, true)
                .putExtra(EXTRA_PICKUP, pickup);
    }

    @NonNull
    public static Intent edit(@NonNull Context context, @NonNull String addressId) {
        return new Intent(context, AddressEditActivity.class)
                .putExtra(EXTRA_EDIT_ID, addressId);
    }

    @NonNull
    public static Intent add(@NonNull Context context) {
        return new Intent(context, AddressEditActivity.class);
    }
}
