package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.WithdrawRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * W03 提现记录 Mock（含 W02 提交写入）
 */
public final class MockWithdrawRepository {

    private static final String PREF = "campusgo_withdraw";
    private static final String KEY_USER_RECORDS = "user_records";

    private MockWithdrawRepository() {
    }

    @NonNull
    public static List<WithdrawRecord> all(@NonNull Context context) {
        List<WithdrawRecord> list = new ArrayList<>();
        list.addAll(loadUserRecords(context));
        list.addAll(seedRecords());
        return list;
    }

    @NonNull
    public static List<WithdrawRecord> filter(@NonNull Context context,
                                              @Nullable WithdrawRecord.Status status) {
        if (status == null) {
            return all(context);
        }
        List<WithdrawRecord> filtered = new ArrayList<>();
        for (WithdrawRecord record : all(context)) {
            if (record.status == status) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    public static void addFromSubmit(@NonNull Context context,
                                     double amount,
                                     @NonNull String accountLabel) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
        WithdrawRecord record = new WithdrawRecord(
                "w" + System.currentTimeMillis(),
                "提现到" + accountLabel,
                time,
                amount,
                WithdrawRecord.Status.PROCESSING);
        append(context, record);
    }

    private static void append(@NonNull Context context, @NonNull WithdrawRecord record) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_USER_RECORDS, "[]"));
        array.put(toJson(record));
        prefs.edit().putString(KEY_USER_RECORDS, array.toString()).apply();
    }

    @NonNull
    private static List<WithdrawRecord> seedRecords() {
        List<WithdrawRecord> list = new ArrayList<>();
        list.add(new WithdrawRecord("s1", "提现到微信零钱", "2024-07-25 14:30",
                100, WithdrawRecord.Status.COMPLETED));
        list.add(new WithdrawRecord("s2", "提现到银行卡", "2024-07-24 09:15",
                500, WithdrawRecord.Status.COMPLETED));
        list.add(new WithdrawRecord("s3", "提现到微信零钱", "2024-07-22 18:45",
                200, WithdrawRecord.Status.COMPLETED));
        list.add(new WithdrawRecord("s4", "提现到微信零钱", "2024-07-25 11:00",
                350, WithdrawRecord.Status.PROCESSING));
        list.add(new WithdrawRecord("s5", "提现到银行卡", "2024-07-20 16:20",
                80, WithdrawRecord.Status.FAILED));
        return list;
    }

    @NonNull
    private static List<WithdrawRecord> loadUserRecords(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_USER_RECORDS, "[]"));
        List<WithdrawRecord> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            WithdrawRecord record = fromJson(array.optJSONObject(i));
            if (record != null) {
                list.add(0, record);
            }
        }
        return list;
    }

    @NonNull
    private static JSONArray readArray(@NonNull String raw) {
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    @NonNull
    private static JSONObject toJson(@NonNull WithdrawRecord record) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", record.id);
            obj.put("title", record.title);
            obj.put("timeLabel", record.timeLabel);
            obj.put("amount", record.amount);
            obj.put("status", record.status.name());
        } catch (JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static WithdrawRecord fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            WithdrawRecord.Status status = WithdrawRecord.Status.valueOf(
                    obj.optString("status", WithdrawRecord.Status.PROCESSING.name()));
            return new WithdrawRecord(
                    obj.optString("id", ""),
                    obj.optString("title", ""),
                    obj.optString("timeLabel", ""),
                    obj.optDouble("amount", 0),
                    status);
        } catch (Exception e) {
            return null;
        }
    }
}
