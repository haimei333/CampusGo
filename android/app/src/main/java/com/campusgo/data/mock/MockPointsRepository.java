package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.PointsTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 积分记录（含签到 / 兑换写入）
 */
public final class MockPointsRepository {

    private static final String PREF = "campusgo_points";
    private static final String KEY_RECORDS = "records";

    private MockPointsRepository() {
    }

    @NonNull
    public static List<PointsTransaction> all(@NonNull Context context) {
        List<PointsTransaction> list = new ArrayList<>();
        list.addAll(seedRecords());
        list.addAll(loadUserRecords(context));
        Collections.sort(list, Comparator.comparing(r -> r.timeLabel));
        Collections.reverse(list);
        return list;
    }

    public static void addEarn(@NonNull Context context, @NonNull String title, int points) {
        if (points <= 0) {
            return;
        }
        append(context, title, points, PointsTransaction.Type.EARN);
    }

    public static void addSpend(@NonNull Context context, @NonNull String title, int points) {
        if (points <= 0) {
            return;
        }
        append(context, title, points, PointsTransaction.Type.SPEND);
    }

    private static void append(@NonNull Context context,
                               @NonNull String title,
                               int points,
                               @NonNull PointsTransaction.Type type) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
        PointsTransaction txn = new PointsTransaction(
                "p" + System.currentTimeMillis(), title, time, points, type);
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_RECORDS, "[]"));
        array.put(toJson(txn));
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply();
    }

    @NonNull
    private static List<PointsTransaction> seedRecords() {
        List<PointsTransaction> list = new ArrayList<>();
        list.add(new PointsTransaction("s1", "新用户奖励", "2024-07-20 10:00", 30,
                PointsTransaction.Type.EARN));
        list.add(new PointsTransaction("s2", "每日签到", "2024-07-25 08:12", 5,
                PointsTransaction.Type.EARN));
        list.add(new PointsTransaction("s3", "连续签到奖励", "2024-07-24 08:05", 5,
                PointsTransaction.Type.EARN));
        list.add(new PointsTransaction("s4", "兑换瑞幸咖啡券", "2024-07-23 14:20", 200,
                PointsTransaction.Type.SPEND));
        list.add(new PointsTransaction("s5", "每日签到", "2024-07-22 09:30", 5,
                PointsTransaction.Type.EARN));
        return list;
    }

    @NonNull
    private static List<PointsTransaction> loadUserRecords(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_RECORDS, "[]"));
        List<PointsTransaction> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            PointsTransaction txn = fromJson(array.optJSONObject(i));
            if (txn != null) {
                list.add(txn);
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
    private static JSONObject toJson(@NonNull PointsTransaction txn) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", txn.id);
            obj.put("title", txn.title);
            obj.put("timeLabel", txn.timeLabel);
            obj.put("points", txn.points);
            obj.put("type", txn.type.name());
        } catch (JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static PointsTransaction fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            PointsTransaction.Type type = PointsTransaction.Type.valueOf(
                    obj.optString("type", PointsTransaction.Type.EARN.name()));
            return new PointsTransaction(
                    obj.optString("id", ""),
                    obj.optString("title", ""),
                    obj.optString("timeLabel", ""),
                    obj.optInt("points", 0),
                    type);
        } catch (Exception e) {
            return null;
        }
    }
}
