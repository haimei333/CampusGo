package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * T03 任务模板 Mock
 */
public final class MockTemplateRepository {

    private static final String PREF = "campusgo_templates";
    private static final String KEY_USER = "user_templates";

    private MockTemplateRepository() {
    }

    @NonNull
    public static List<TaskTemplate> systemTemplates() {
        List<TaskTemplate> list = new ArrayList<>();
        list.add(new TaskTemplate(
                "sys_express", TaskTemplate.Source.SYSTEM,
                "取快递", "帮我取件",
                "取快递 - 菜鸟驿站", "请帮忙取一个小件快递",
                TaskMode.NORMAL, TaskCategory.EXPRESS,
                "菜鸟驿站 · 东门", "38号楼 512室 · 海淀区颐和园路5号北京大学",
                "尽快", 5.0, "📦"));
        list.add(new TaskTemplate(
                "sys_milktea", TaskTemplate.Source.SYSTEM,
                "代买奶茶", "帮我购买",
                "代买奶茶", "蜜雪冰城一杯，少冰",
                TaskMode.NORMAL, TaskCategory.BUY,
                "蜜雪冰城 · 二食堂", "38号楼 512室 · 海淀区颐和园路5号北京大学",
                "1小时内", 12.0, "🧋"));
        list.add(new TaskTemplate(
                "sys_file", TaskTemplate.Source.SYSTEM,
                "送文件", "帮我送达",
                "送文件到行政楼", "密封文件，需当面交接",
                TaskMode.NORMAL, TaskCategory.ERRAND,
                "图书馆 2层自习室 · 海淀区颐和园路5号北京大学",
                "行政楼 3层 · 海淀区颐和园路5号北京大学",
                "2小时内", 15.0, "📄"));
        return list;
    }

    @NonNull
    public static List<TaskTemplate> userTemplates(@NonNull Context context) {
        ensureUserSeed(context);
        return loadUser(context);
    }

    @Nullable
    public static TaskTemplate findById(@NonNull Context context, @NonNull String id) {
        for (TaskTemplate template : systemTemplates()) {
            if (template.id.equals(id)) {
                return template;
            }
        }
        for (TaskTemplate template : userTemplates(context)) {
            if (template.id.equals(id)) {
                return template;
            }
        }
        return null;
    }

    public static void addUser(@NonNull Context context, @NonNull TaskTemplate template) {
        List<TaskTemplate> list = new ArrayList<>(loadUser(context));
        list.add(template);
        saveUser(context, list);
    }

    public static void deleteUser(@NonNull Context context, @NonNull String id) {
        List<TaskTemplate> list = new ArrayList<>();
        for (TaskTemplate template : loadUser(context)) {
            if (!template.id.equals(id)) {
                list.add(template);
            }
        }
        saveUser(context, list);
    }

    private static void ensureUserSeed(@NonNull Context context) {
        if (!loadUser(context).isEmpty()) {
            return;
        }
        List<TaskTemplate> seed = new ArrayList<>();
        seed.add(new TaskTemplate(
                "u_zto", TaskTemplate.Source.USER,
                "取快递-中通", "中通快递站取件",
                "取快递 - 中通", "中通小件，取件码已私聊",
                TaskMode.NORMAL, TaskCategory.EXPRESS,
                "南门快递站 · 中通", "38号楼 512室 · 海淀区颐和园路5号北京大学",
                "尽快", 8.0, "📦"));
        seed.add(new TaskTemplate(
                "u_breakfast", TaskTemplate.Source.USER,
                "买早餐", "食堂早餐代买",
                "买早餐", "农园食堂包子+豆浆",
                TaskMode.NORMAL, TaskCategory.BUY,
                "农园食堂 1层 · 海淀区颐和园路5号北京大学",
                "38号楼 512室 · 海淀区颐和园路5号北京大学",
                "30分钟内", 6.0, "🥐"));
        saveUser(context, seed);
    }

    @NonNull
    private static List<TaskTemplate> loadUser(@NonNull Context context) {
        JSONArray array = readArray(prefs(context).getString(KEY_USER, "[]"));
        List<TaskTemplate> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            TaskTemplate template = fromJson(array.optJSONObject(i));
            if (template != null) {
                list.add(template);
            }
        }
        return list;
    }

    private static void saveUser(@NonNull Context context, @NonNull List<TaskTemplate> list) {
        JSONArray array = new JSONArray();
        for (TaskTemplate template : list) {
            array.put(toJson(template));
        }
        prefs(context).edit().putString(KEY_USER, array.toString()).apply();
    }

    @NonNull
    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
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
    private static JSONObject toJson(@NonNull TaskTemplate template) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", template.id);
            obj.put("name", template.name);
            obj.put("subtitle", template.subtitle);
            obj.put("title", template.title);
            obj.put("description", template.description);
            obj.put("mode", template.mode.name());
            obj.put("category", template.category.name());
            obj.put("pickupAddress", template.pickupAddress);
            obj.put("deliveryAddress", template.deliveryAddress);
            obj.put("timeLabel", template.timeLabel);
            obj.put("reward", template.reward);
            obj.put("iconEmoji", template.iconEmoji);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static TaskTemplate fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            return new TaskTemplate(
                    obj.optString("id", ""),
                    TaskTemplate.Source.USER,
                    obj.optString("name", ""),
                    obj.optString("subtitle", ""),
                    obj.optString("title", ""),
                    obj.optString("description", ""),
                    TaskMode.valueOf(obj.optString("mode", TaskMode.NORMAL.name())),
                    TaskCategory.valueOf(obj.optString("category", TaskCategory.EXPRESS.name())),
                    obj.optString("pickupAddress", ""),
                    obj.optString("deliveryAddress", ""),
                    obj.optString("timeLabel", ""),
                    obj.optDouble("reward", 0),
                    obj.optString("iconEmoji", "📋"));
        } catch (Exception e) {
            return null;
        }
    }
}
