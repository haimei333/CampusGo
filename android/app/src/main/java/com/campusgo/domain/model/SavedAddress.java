package com.campusgo.domain.model;

import androidx.annotation.NonNull;

/**
 * T04 常用地址
 */
public final class SavedAddress {

    public enum Type {
        DORM,
        BUILDING,
        LIBRARY,
        CANTEEN,
        OTHER
    }

    @NonNull
    public final String id;
    @NonNull
    public final Type type;
    @NonNull
    public final String title;
    @NonNull
    public final String detail;
    public final boolean isDefault;

    public SavedAddress(@NonNull String id,
                        @NonNull Type type,
                        @NonNull String title,
                        @NonNull String detail,
                        boolean isDefault) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.detail = detail;
        this.isDefault = isDefault;
    }

    @NonNull
    public String formatShort() {
        return title;
    }

    @NonNull
    public String formatFull() {
        if (detail.isEmpty()) {
            return title;
        }
        return title + " · " + detail;
    }
}
