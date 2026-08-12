package com.campusgo.ui.settings;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * S05 设置相关跳转
 */
public final class SettingsNavigator {

    private SettingsNavigator() {
    }

    @NonNull
    public static Intent security(@NonNull Context context) {
        return new Intent(context, SecurityActivity.class);
    }

    @NonNull
    public static Intent privacy(@NonNull Context context) {
        return legal(context, LegalDocumentActivity.TYPE_PRIVACY);
    }

    @NonNull
    public static Intent agreement(@NonNull Context context) {
        return legal(context, LegalDocumentActivity.TYPE_AGREEMENT);
    }

    @NonNull
    private static Intent legal(@NonNull Context context, @NonNull String type) {
        return new Intent(context, LegalDocumentActivity.class)
                .putExtra(LegalDocumentActivity.EXTRA_DOC_TYPE, type);
    }
}
