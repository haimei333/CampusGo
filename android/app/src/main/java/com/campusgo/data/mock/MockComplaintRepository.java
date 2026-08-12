package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.ComplaintRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * S03 投诉记录演示数据（含 S02 提交持久化）
 */
public final class MockComplaintRepository {

    private static final String PREF = "campusgo_complaints";
    private static final String KEY_USER_RECORDS = "user_records";

    private MockComplaintRepository() {
    }

    @NonNull
    public static List<ComplaintRecord> all(@NonNull Context context) {
        List<ComplaintRecord> list = new ArrayList<>();
        list.addAll(loadUserRecords(context));
        list.addAll(seedRecords());
        return list;
    }

    @Nullable
    public static ComplaintRecord findById(@NonNull Context context, @NonNull String id) {
        for (ComplaintRecord record : all(context)) {
            if (record.id.equals(id)) {
                return record;
            }
        }
        return null;
    }

    public static void addFromSubmit(@NonNull Context context,
                                     @NonNull String type,
                                     @NonNull String taskTitle,
                                     @NonNull String description) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
        ComplaintRecord record = new ComplaintRecord(
                "u" + System.currentTimeMillis(),
                type,
                taskTitle,
                description,
                time,
                ComplaintRecord.Status.PENDING);
        appendUserRecord(context, record);
    }

    @NonNull
    private static List<ComplaintRecord> seedRecords() {
        List<ComplaintRecord> list = new ArrayList<>();
        list.add(new ComplaintRecord("c1", "物品损坏", "取快递 - 中通快递",
                "快递包装破损，内部物品有损坏，需要申请赔偿处理...",
                "2024-07-25 15:30", ComplaintRecord.Status.PENDING));
        list.add(new ComplaintRecord("c2", "服务态度差", "送外卖 - 食堂二楼",
                "配送员态度恶劣，送餐延迟且未提前通知，已与对方沟通...",
                "2024-07-24 12:15", ComplaintRecord.Status.DONE));
        list.add(new ComplaintRecord("c3", "物品丢失", "代取包裹 - 顺丰快递",
                "快递显示已签收但实际未收到，经核实为系统误报，已驳回...",
                "2024-07-23 09:40", ComplaintRecord.Status.REJECTED));
        list.add(new ComplaintRecord("c4", "费用争议", "代购商品 - 便利店",
                "实际收费与标注价格不符，多收取了5元跑腿费，等待平台核实...",
                "2024-07-22 17:55", ComplaintRecord.Status.PENDING));
        return list;
    }

    private static void appendUserRecord(@NonNull Context context, @NonNull ComplaintRecord record) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_USER_RECORDS, "[]"));
        array.put(toJson(record));
        prefs.edit().putString(KEY_USER_RECORDS, array.toString()).apply();
    }

    @NonNull
    private static List<ComplaintRecord> loadUserRecords(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        JSONArray array = readArray(prefs.getString(KEY_USER_RECORDS, "[]"));
        List<ComplaintRecord> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            ComplaintRecord record = fromJson(array.optJSONObject(i));
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
    private static JSONObject toJson(@NonNull ComplaintRecord record) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", record.id);
            obj.put("type", record.type);
            obj.put("taskTitle", record.taskTitle);
            obj.put("description", record.description);
            obj.put("timeLabel", record.timeLabel);
            obj.put("status", record.status.name());
        } catch (JSONException ignored) {
        }
        return obj;
    }

    private static ComplaintRecord fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            ComplaintRecord.Status status = ComplaintRecord.Status.valueOf(
                    obj.optString("status", ComplaintRecord.Status.PENDING.name()));
            return new ComplaintRecord(
                    obj.optString("id", ""),
                    obj.optString("type", ""),
                    obj.optString("taskTitle", ""),
                    obj.optString("description", ""),
                    obj.optString("timeLabel", ""),
                    status);
        } catch (Exception e) {
            return null;
        }
    }
}
