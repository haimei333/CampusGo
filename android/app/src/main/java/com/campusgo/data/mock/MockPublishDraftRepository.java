package com.campusgo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.PublishDraft;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * T01 发布草稿持久化
 */
public final class MockPublishDraftRepository {

    private static final String PREF = "campusgo_publish_drafts";
    private static final String KEY_LIST = "drafts";
    public static final String DEFAULT_DRAFT_ID = "p1";

    private MockPublishDraftRepository() {
    }

    @NonNull
    public static List<PublishDraft> all(@NonNull Context context) {
        ensureSeed(context);
        return load(context);
    }

    @Nullable
    public static PublishDraft findById(@NonNull Context context, @NonNull String id) {
        for (PublishDraft draft : all(context)) {
            if (draft.id.equals(id)) {
                return draft;
            }
        }
        return null;
    }

    @NonNull
    public static List<TaskListItem> draftListItems(@NonNull Context context) {
        List<TaskListItem> items = new ArrayList<>();
        for (PublishDraft draft : all(context)) {
            items.add(new TaskListItem(
                    draft.id,
                    TaskListItem.Tab.MINE_PUBLISH,
                    null,
                    "草稿 · " + draft.listTitle(),
                    "草稿",
                    draft.listSubtitle(),
                    draft.reward > 0 ? String.format("¥%.2f", draft.reward) : null,
                    draft.category,
                    draft.mode,
                    TaskStatus.DRAFT,
                    TaskListItem.NavTarget.T01));
        }
        return items;
    }

    @NonNull
    public static PublishDraft save(@NonNull Context context, @NonNull PublishDraft draft) {
        List<PublishDraft> list = new ArrayList<>();
        boolean replaced = false;
        for (PublishDraft item : loadRaw(context)) {
            if (item.id.equals(draft.id)) {
                list.add(draft);
                replaced = true;
            } else {
                list.add(item);
            }
        }
        if (!replaced) {
            list.add(draft);
        }
        persist(context, list);
        return draft;
    }

    public static void delete(@NonNull Context context, @NonNull String id) {
        List<PublishDraft> list = new ArrayList<>();
        for (PublishDraft item : loadRaw(context)) {
            if (!item.id.equals(id)) {
                list.add(item);
            }
        }
        persist(context, list);
    }

    private static void ensureSeed(@NonNull Context context) {
        if (!loadRaw(context).isEmpty()) {
            return;
        }
        List<PublishDraft> seed = new ArrayList<>();
        seed.add(new PublishDraft(
                DEFAULT_DRAFT_ID,
                "取快递 - 中通",
                "小件，菜鸟驿站取件",
                TaskMode.NORMAL,
                TaskCategory.EXPRESS,
                "菜鸟驿站 · 东门",
                "38号楼 512室 · 海淀区颐和园路5号北京大学",
                "尽快",
                15.0,
                System.currentTimeMillis()));
        persist(context, seed);
    }

    @NonNull
    private static List<PublishDraft> load(@NonNull Context context) {
        List<PublishDraft> list = new ArrayList<>(loadRaw(context));
        Collections.sort(list, Comparator.comparingLong(d -> -d.updatedAt));
        return list;
    }

    @NonNull
    private static List<PublishDraft> loadRaw(@NonNull Context context) {
        SharedPreferences prefs = prefs(context);
        JSONArray array = readArray(prefs.getString(KEY_LIST, "[]"));
        List<PublishDraft> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            PublishDraft draft = fromJson(array.optJSONObject(i));
            if (draft != null) {
                list.add(draft);
            }
        }
        return list;
    }

    private static void persist(@NonNull Context context, @NonNull List<PublishDraft> list) {
        JSONArray array = new JSONArray();
        for (PublishDraft draft : list) {
            array.put(toJson(draft));
        }
        prefs(context).edit().putString(KEY_LIST, array.toString()).apply();
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
    private static JSONObject toJson(@NonNull PublishDraft draft) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", draft.id);
            obj.put("title", draft.title);
            obj.put("description", draft.description);
            obj.put("mode", draft.mode.name());
            obj.put("category", draft.category.name());
            obj.put("pickupAddress", draft.pickupAddress);
            obj.put("deliveryAddress", draft.deliveryAddress);
            obj.put("timeLabel", draft.timeLabel);
            obj.put("reward", draft.reward);
            obj.put("updatedAt", draft.updatedAt);
        } catch (JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static PublishDraft fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            return new PublishDraft(
                    obj.optString("id", ""),
                    obj.optString("title", ""),
                    obj.optString("description", ""),
                    TaskMode.valueOf(obj.optString("mode", TaskMode.NORMAL.name())),
                    TaskCategory.valueOf(obj.optString("category", TaskCategory.EXPRESS.name())),
                    obj.optString("pickupAddress", ""),
                    obj.optString("deliveryAddress", ""),
                    obj.optString("timeLabel", ""),
                    obj.optDouble("reward", 0),
                    obj.optLong("updatedAt", System.currentTimeMillis()));
        } catch (Exception e) {
            return null;
        }
    }
}
