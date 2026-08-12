package com.campusgo.ui.wallet;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * 钱包 / 签到 / 认证跳转
 */
public final class WalletNavigator {

    public static final String EXTRA_SUGGEST_AMOUNT = "suggest_amount";
    public static final String EXTRA_TOPUP_RESULT = "topup_amount";

    private WalletNavigator() {
    }

    @NonNull
    public static Intent wallet(@NonNull Context context) {
        return new Intent(context, WalletActivity.class);
    }

    @NonNull
    public static Intent topUp(@NonNull Context context) {
        return new Intent(context, TopUpActivity.class);
    }

    @NonNull
    public static Intent topUp(@NonNull Context context, double suggestAmount) {
        return topUp(context).putExtra(EXTRA_SUGGEST_AMOUNT, suggestAmount);
    }

    @NonNull
    public static Intent verify(@NonNull Context context) {
        return new Intent(context, com.campusgo.ui.auth.VerifyActivity.class);
    }

    @NonNull
    public static Intent checkIn(@NonNull Context context) {
        return new Intent(context, CheckInActivity.class);
    }

    @NonNull
    public static Intent withdraw(@NonNull Context context) {
        return new Intent(context, WithdrawActivity.class);
    }

    @NonNull
    public static Intent withdrawAccount(@NonNull Context context) {
        return new Intent(context, WithdrawAccountActivity.class);
    }

    @NonNull
    public static Intent withdrawRecords(@NonNull Context context) {
        return new Intent(context, WithdrawRecordsActivity.class);
    }
}
