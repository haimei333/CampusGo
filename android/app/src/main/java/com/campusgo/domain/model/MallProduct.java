package com.campusgo.domain.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/**
 * G01 积分商城商品
 */
public final class MallProduct {

    public enum Category {
        ALL,
        VOUCHER,
        GOODS,
        FLASH
    }

    @NonNull
    public final String id;
    @NonNull
    public final String name;
    @NonNull
    public final String subtitle;
    @NonNull
    public final String emoji;
    public final int pointsCost;
    @NonNull
    public final Category category;
    @DrawableRes
    public final int imageBgRes;
    public final boolean flashSale;
    public final int originalPoints;

    public MallProduct(@NonNull String id,
                       @NonNull String name,
                       @NonNull String subtitle,
                       @NonNull String emoji,
                       int pointsCost,
                       @NonNull Category category,
                       @DrawableRes int imageBgRes) {
        this(id, name, subtitle, emoji, pointsCost, category, imageBgRes, false, 0);
    }

    public MallProduct(@NonNull String id,
                       @NonNull String name,
                       @NonNull String subtitle,
                       @NonNull String emoji,
                       int pointsCost,
                       @NonNull Category category,
                       @DrawableRes int imageBgRes,
                       boolean flashSale,
                       int originalPoints) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.emoji = emoji;
        this.pointsCost = pointsCost;
        this.category = category;
        this.imageBgRes = imageBgRes;
        this.flashSale = flashSale;
        this.originalPoints = originalPoints;
    }
}
