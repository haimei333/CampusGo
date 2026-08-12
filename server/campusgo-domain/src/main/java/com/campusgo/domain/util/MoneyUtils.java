package com.campusgo.domain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static String formatYuan(int cent) {
        return BigDecimal.valueOf(cent, 2)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
