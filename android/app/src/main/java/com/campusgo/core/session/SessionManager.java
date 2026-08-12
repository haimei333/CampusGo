package com.campusgo.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.dto.auth.LoginResponse;
import com.campusgo.data.remote.dto.user.UserProfileDto;
import com.campusgo.data.remote.dto.wallet.WalletResponse;
import com.campusgo.domain.model.UserRole;

/**
 * 本地会话：Token、登录态、当前身份（MVP 用 SharedPreferences）
 */
public class SessionManager {

    private static final String PREF = "campusgo_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "active_role";
    private static final String KEY_CREDIT = "credit_score";
    private static final String KEY_VERIFIED = "campus_verified";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_WALLET_BALANCE = "wallet_balance";
    private static final String KEY_WALLET_INCOME = "wallet_total_income";
    private static final String KEY_WALLET_FROZEN = "wallet_frozen";
    private static final String KEY_POINTS = "points";
    private static final String KEY_CHECKIN_STREAK = "checkin_streak";
    private static final String KEY_CHECKIN_TODAY = "checkin_today";
    private static final String KEY_GUIDE_SHOWN = "guide_shown";
    private static final String KEY_WITHDRAW_BOUND = "withdraw_bound";
    private static final String KEY_WITHDRAW_TYPE = "withdraw_type";
    private static final String KEY_WITHDRAW_MASK = "withdraw_mask";

    public static final String WITHDRAW_WECHAT = "WECHAT";
    public static final String WITHDRAW_ALIPAY = "ALIPAY";
    public static final String WITHDRAW_BANK = "BANK";

    private static final String KEY_NOTIFY_MESSAGE = "notify_message";
    private static final String KEY_NOTIFY_SOUND = "notify_sound";
    private static final String KEY_NOTIFY_VIBRATE = "notify_vibrate";
    private static final String KEY_CACHE_MB = "cache_mb";
    /** Mock 模式本地已注册账号：phone -> password */
    private static final String PREF_LOCAL_ACCOUNTS = "campusgo_local_accounts";

    /** 规格假数据：昵称「小林」 */
    public static final String DEMO_NICKNAME = "小林";
    public static final double DEMO_WALLET_BALANCE = 128.50;
    public static final double DEMO_WALLET_INCOME = 1256.00;

    private final SharedPreferences prefs;
    private final SharedPreferences localAccounts;

    public SessionManager(@NonNull Context context) {
        Context app = context.getApplicationContext();
        prefs = app.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        localAccounts = app.getSharedPreferences(PREF_LOCAL_ACCOUNTS, Context.MODE_PRIVATE);
        // 演示账号预置（仅 Mock 模式使用）
        if (!localAccounts.contains("13800138000")) {
            localAccounts.edit().putString("13800138000", "123456").apply();
        }
    }

    /** Mock：注册本地账号；已存在返回 false */
    public boolean registerLocalAccount(@NonNull String phone, @NonNull String password) {
        if (localAccounts.contains(phone)) {
            return false;
        }
        localAccounts.edit().putString(phone, password).apply();
        return true;
    }

    /** Mock：校验本地账号密码；不存在返回 null，密码错返回 false */
    @Nullable
    public Boolean verifyLocalAccount(@NonNull String phone, @NonNull String password) {
        if (!localAccounts.contains(phone)) {
            return null;
        }
        String saved = localAccounts.getString(phone, null);
        return saved != null && saved.equals(password);
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_TOKEN, null);
        return token != null && !token.isEmpty();
    }

    public void login(@NonNull String phone, @NonNull String mockToken) {
        // 未认证：便于展示 BannerAuth / DIALOG-AUTH（演示路径）
        prefs.edit()
                .putString(KEY_PHONE, phone)
                .putString(KEY_TOKEN, mockToken)
                .remove(KEY_REFRESH_TOKEN)
                .putString(KEY_NICKNAME, DEMO_NICKNAME)
                .putString(KEY_ROLE, UserRole.PUBLISHER.name())
                .putInt(KEY_CREDIT, 720)
                .putBoolean(KEY_VERIFIED, false)
                .putFloat(KEY_WALLET_BALANCE, (float) DEMO_WALLET_BALANCE)
                .putFloat(KEY_WALLET_INCOME, (float) DEMO_WALLET_INCOME)
                .putInt(KEY_POINTS, 30)
                .putInt(KEY_CHECKIN_STREAK, 6)
                .putBoolean(KEY_CHECKIN_TODAY, false)
                .apply();
    }

    /** 远程登录成功后写入会话（JWT + 用户资料） */
    public void applyRemoteLogin(@NonNull String phone, @NonNull LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_PHONE, phone)
                .putString(KEY_TOKEN, response.accessToken != null ? response.accessToken : "")
                .putString(KEY_REFRESH_TOKEN,
                        response.refreshToken != null ? response.refreshToken : "");

        UserProfileDto profile = response.userProfile;
        if (profile != null) {
            if (profile.nickname != null && !profile.nickname.isEmpty()) {
                editor.putString(KEY_NICKNAME, profile.nickname);
            }
            if (profile.activeRole != null) {
                editor.putString(KEY_ROLE, profile.activeRole.name());
            }
            editor.putInt(KEY_CREDIT, profile.creditScore);
            editor.putBoolean(KEY_VERIFIED, isCampusApproved(profile.campusStatus));
        }
        editor.apply();
    }

    private static boolean isCampusApproved(@Nullable String campusStatus) {
        return "APPROVED".equals(campusStatus);
    }

    /** 同步用户资料到本地会话 */
    public void applyUserProfile(@NonNull UserProfileDto profile) {
        SharedPreferences.Editor editor = prefs.edit();
        if (profile.nickname != null && !profile.nickname.isEmpty()) {
            editor.putString(KEY_NICKNAME, profile.nickname);
        }
        if (profile.activeRole != null) {
            editor.putString(KEY_ROLE, profile.activeRole.name());
        }
        editor.putInt(KEY_CREDIT, profile.creditScore);
        editor.putBoolean(KEY_VERIFIED, isCampusApproved(profile.campusStatus));
        editor.apply();
    }

    /** 同步钱包余额到本地会话 */
    public void applyWallet(@NonNull WalletResponse wallet) {
        prefs.edit()
                .putFloat(KEY_WALLET_BALANCE, (float) (wallet.balanceCent / 100.0))
                .putFloat(KEY_WALLET_INCOME, (float) (wallet.totalIncomeCent / 100.0))
                .putFloat(KEY_WALLET_FROZEN, (float) (wallet.frozenCent / 100.0))
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    @Nullable
    public String getPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public void setPhone(@NonNull String phone) {
        prefs.edit().putString(KEY_PHONE, phone).apply();
    }

    public boolean isNotifyMessageEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_MESSAGE, true);
    }

    public void setNotifyMessageEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_MESSAGE, enabled).apply();
    }

    public boolean isNotifySoundEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    public void setNotifySoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_SOUND, enabled).apply();
    }

    public boolean isNotifyVibrateEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_VIBRATE, false);
    }

    public void setNotifyVibrateEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_VIBRATE, enabled).apply();
    }

    public float getCacheSizeMb() {
        return prefs.getFloat(KEY_CACHE_MB, 23.5f);
    }

    public void clearCache() {
        prefs.edit().putFloat(KEY_CACHE_MB, 0f).apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    @NonNull
    public UserRole getActiveRole() {
        String raw = prefs.getString(KEY_ROLE, UserRole.PUBLISHER.name());
        try {
            return UserRole.valueOf(raw);
        } catch (Exception e) {
            return UserRole.PUBLISHER;
        }
    }

    public void setActiveRole(@NonNull UserRole role) {
        prefs.edit().putString(KEY_ROLE, role.name()).apply();
    }

    public int getCreditScore() {
        return prefs.getInt(KEY_CREDIT, 500);
    }

    public boolean isCampusVerified() {
        return prefs.getBoolean(KEY_VERIFIED, false);
    }

    public void setCampusVerified(boolean verified) {
        prefs.edit().putBoolean(KEY_VERIFIED, verified).apply();
    }

    public double getWalletBalance() {
        return prefs.getFloat(KEY_WALLET_BALANCE, (float) DEMO_WALLET_BALANCE);
    }

    public double getTotalIncome() {
        return prefs.getFloat(KEY_WALLET_INCOME, (float) DEMO_WALLET_INCOME);
    }

    public double getFrozenBalance() {
        return prefs.getFloat(KEY_WALLET_FROZEN, 0f);
    }

    public void addWalletBalance(double amount) {
        if (amount <= 0) {
            return;
        }
        double next = getWalletBalance() + amount;
        prefs.edit().putFloat(KEY_WALLET_BALANCE, (float) next).apply();
    }

    public boolean deductWalletBalance(double amount) {
        if (amount <= 0) {
            return true;
        }
        double balance = getWalletBalance();
        if (balance + 0.001 < amount) {
            return false;
        }
        prefs.edit().putFloat(KEY_WALLET_BALANCE, (float) (balance - amount)).apply();
        return true;
    }

    public int getPoints() {
        return prefs.getInt(KEY_POINTS, 30);
    }

    public void setPoints(int points) {
        prefs.edit().putInt(KEY_POINTS, points).apply();
    }

    public void addPoints(int points) {
        if (points <= 0) {
            return;
        }
        prefs.edit().putInt(KEY_POINTS, getPoints() + points).apply();
    }

    public boolean deductPoints(int points) {
        if (points <= 0) {
            return true;
        }
        int balance = getPoints();
        if (balance < points) {
            return false;
        }
        prefs.edit().putInt(KEY_POINTS, balance - points).apply();
        return true;
    }

    public int getCheckInStreak() {
        return prefs.getInt(KEY_CHECKIN_STREAK, 0);
    }

    public boolean isCheckedInToday() {
        return prefs.getBoolean(KEY_CHECKIN_TODAY, false);
    }

    public void setCheckInStreak(int streak) {
        prefs.edit().putInt(KEY_CHECKIN_STREAK, streak).apply();
    }

    public void setCheckedInToday(boolean checkedIn) {
        prefs.edit().putBoolean(KEY_CHECKIN_TODAY, checkedIn).apply();
    }

    /** @return 本次签到获得的积分 */
    public int checkInToday() {
        if (isCheckedInToday()) {
            return 0;
        }
        int streak = getCheckInStreak() + 1;
        prefs.edit()
                .putInt(KEY_CHECKIN_STREAK, streak)
                .putBoolean(KEY_CHECKIN_TODAY, true)
                .apply();
        int reward = 5;
        addPoints(reward);
        return reward;
    }

    @NonNull
    public String formatWalletBalance() {
        return String.format("¥%.2f", getWalletBalance());
    }

    @NonNull
    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, DEMO_NICKNAME);
    }

    public void setNickname(@NonNull String nickname) {
        String value = nickname.trim();
        if (value.isEmpty()) {
            return;
        }
        prefs.edit().putString(KEY_NICKNAME, value).apply();
    }

    @NonNull
    public String getAvatarInitial() {
        String nick = getNickname();
        return nick.isEmpty() ? "?" : nick.substring(0, 1).toUpperCase();
    }

    public boolean isGuideShown() {
        return prefs.getBoolean(KEY_GUIDE_SHOWN, false);
    }

    public void setGuideShown(boolean shown) {
        prefs.edit().putBoolean(KEY_GUIDE_SHOWN, shown).apply();
    }

    public void resetGuide() {
        prefs.edit().putBoolean(KEY_GUIDE_SHOWN, false).apply();
    }

    public boolean isWithdrawAccountBound() {
        return prefs.getBoolean(KEY_WITHDRAW_BOUND, false);
    }

    @Nullable
    public String getWithdrawAccountType() {
        return prefs.getString(KEY_WITHDRAW_TYPE, null);
    }

    @Nullable
    public String getWithdrawAccountMask() {
        return prefs.getString(KEY_WITHDRAW_MASK, null);
    }

    public void bindWithdrawAccount(@NonNull String type, @NonNull String mask) {
        prefs.edit()
                .putBoolean(KEY_WITHDRAW_BOUND, true)
                .putString(KEY_WITHDRAW_TYPE, type)
                .putString(KEY_WITHDRAW_MASK, mask)
                .apply();
    }

    public void unbindWithdrawAccount() {
        prefs.edit()
                .putBoolean(KEY_WITHDRAW_BOUND, false)
                .remove(KEY_WITHDRAW_TYPE)
                .remove(KEY_WITHDRAW_MASK)
                .apply();
    }

    @NonNull
    public String getWithdrawAccountDisplay() {
        if (!isWithdrawAccountBound()) {
            return "";
        }
        String type = getWithdrawAccountType();
        String mask = getWithdrawAccountMask();
        if (type == null || mask == null) {
            return "";
        }
        return withdrawTypeLabel(type) + " (" + mask + ")";
    }

    @NonNull
    public String withdrawTypeLabel(@NonNull String type) {
        switch (type) {
            case WITHDRAW_ALIPAY:
                return "支付宝";
            case WITHDRAW_BANK:
                return "银行卡";
            case WITHDRAW_WECHAT:
            default:
                return "微信";
        }
    }

    @NonNull
    public String defaultWithdrawMask() {
        String phone = getPhone();
        if (phone != null && phone.length() >= 4) {
            return "****" + phone.substring(phone.length() - 4);
        }
        return "****6789";
    }

    /**
     * 切跑腿员门禁：未认证 / 信用分 &lt; 400
     */
    public SwitchRoleResult canSwitchToRunner() {
        if (!isCampusVerified()) {
            return SwitchRoleResult.NEED_VERIFY;
        }
        if (getCreditScore() < 400) {
            return SwitchRoleResult.CREDIT_BLOCKED;
        }
        return SwitchRoleResult.ALLOWED;
    }

    public enum SwitchRoleResult {
        ALLOWED,
        NEED_VERIFY,
        CREDIT_BLOCKED
    }
}
