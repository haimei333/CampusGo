package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.SavedAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * T04 地址簿 Mock（本地持久化）
 */
public final class MockAddressRepository {

    private static final String PREF = "campusgo_addresses";
    private static final String KEY_LIST = "list";

    private MockAddressRepository() {
    }

    @NonNull
    public static List<SavedAddress> all(@NonNull Context context) {
        List<SavedAddress> list = load(context);
        if (list.isEmpty()) {
            list = seed();
            save(context, list);
        }
        return list;
    }

    @Nullable
    public static SavedAddress findById(@NonNull Context context, @NonNull String id) {
        for (SavedAddress address : all(context)) {
            if (address.id.equals(id)) {
                return address;
            }
        }
        return null;
    }

    @Nullable
    public static SavedAddress getDefault(@NonNull Context context) {
        for (SavedAddress address : all(context)) {
            if (address.isDefault) {
                return address;
            }
        }
        List<SavedAddress> list = all(context);
        return list.isEmpty() ? null : list.get(0);
    }

    @NonNull
    public static String[] displayOptions(@NonNull Context context) {
        List<SavedAddress> list = all(context);
        String[] options = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            options[i] = list.get(i).formatFull();
        }
        return options;
    }

    public static void add(@NonNull Context context, @NonNull SavedAddress address) {
        List<SavedAddress> list = new ArrayList<>(all(context));
        if (address.isDefault) {
            list = clearDefault(list);
        }
        list.add(address);
        save(context, list);
    }

    public static void update(@NonNull Context context, @NonNull SavedAddress address) {
        List<SavedAddress> list = new ArrayList<>();
        for (SavedAddress item : all(context)) {
            if (item.id.equals(address.id)) {
                list.add(address);
            } else {
                list.add(address.isDefault
                        ? copyWithoutDefault(item)
                        : item);
            }
        }
        save(context, list);
    }

    public static void delete(@NonNull Context context, @NonNull String id) {
        List<SavedAddress> list = new ArrayList<>();
        for (SavedAddress item : all(context)) {
            if (!item.id.equals(id)) {
                list.add(item);
            }
        }
        save(context, list);
    }

    @NonNull
    private static List<SavedAddress> seed() {
        List<SavedAddress> list = new ArrayList<>();
        list.add(new SavedAddress("a1", SavedAddress.Type.DORM,
                "38号楼 512室", "海淀区颐和园路5号北京大学", true));
        list.add(new SavedAddress("a2", SavedAddress.Type.BUILDING,
                "理科教学楼 301教室", "海淀区颐和园路5号北京大学", false));
        list.add(new SavedAddress("a3", SavedAddress.Type.LIBRARY,
                "图书馆 2层自习室", "海淀区颐和园路5号北京大学", false));
        list.add(new SavedAddress("a4", SavedAddress.Type.CANTEEN,
                "农园食堂 1层", "海淀区颐和园路5号北京大学", false));
        return list;
    }

    @NonNull
    private static List<SavedAddress> load(@NonNull Context context) {
        SharedPreferences prefs = prefs(context);
        JSONArray array = readArray(prefs.getString(KEY_LIST, "[]"));
        List<SavedAddress> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            SavedAddress address = fromJson(array.optJSONObject(i));
            if (address != null) {
                list.add(address);
            }
        }
        return list;
    }

    private static void save(@NonNull Context context, @NonNull List<SavedAddress> list) {
        JSONArray array = new JSONArray();
        for (SavedAddress address : list) {
            array.put(toJson(address));
        }
        prefs(context).edit().putString(KEY_LIST, array.toString()).apply();
    }

    @NonNull
    private static List<SavedAddress> clearDefault(@NonNull List<SavedAddress> list) {
        List<SavedAddress> cleared = new ArrayList<>();
        for (SavedAddress item : list) {
            cleared.add(copyWithoutDefault(item));
        }
        return cleared;
    }

    @NonNull
    private static SavedAddress copyWithoutDefault(@NonNull SavedAddress item) {
        return new SavedAddress(item.id, item.type, item.title, item.detail, false);
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
    private static JSONObject toJson(@NonNull SavedAddress address) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", address.id);
            obj.put("type", address.type.name());
            obj.put("title", address.title);
            obj.put("detail", address.detail);
            obj.put("isDefault", address.isDefault);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static SavedAddress fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            SavedAddress.Type type = SavedAddress.Type.valueOf(
                    obj.optString("type", SavedAddress.Type.OTHER.name()));
            return new SavedAddress(
                    obj.optString("id", ""),
                    type,
                    obj.optString("title", ""),
                    obj.optString("detail", ""),
                    obj.optBoolean("isDefault", false));
        } catch (Exception e) {
            return null;
        }
    }
}
