package com.campusgo.ui.profile;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.campusgo.ui.ai.AiChatActivity;

/**
 * 我的页二级跳转：看板 / 商城 / 投诉记录 / AI 助手
 */
public final class ProfileNavigator {

    public static final String EXTRA_COMPLAINT_ID = "complaint_id";

    private ProfileNavigator() {
    }

    @NonNull
    public static Intent dashboard(@NonNull Context context) {
        return new Intent(context, DashboardActivity.class);
    }

    @NonNull
    public static Intent mall(@NonNull Context context) {
        return new Intent(context, MallActivity.class);
    }

    @NonNull
    public static Intent complaintRecords(@NonNull Context context) {
        return new Intent(context, ComplaintRecordsActivity.class);
    }

    @NonNull
    public static Intent pointsHistory(@NonNull Context context) {
        return new Intent(context, PointsHistoryActivity.class);
    }

    @NonNull
    public static Intent complaintDetail(@NonNull Context context, @NonNull String recordId) {
        return new Intent(context, ComplaintDetailActivity.class)
                .putExtra(EXTRA_COMPLAINT_ID, recordId);
    }

    @NonNull
    public static Intent help(@NonNull Context context) {
        return new Intent(context, com.campusgo.ui.help.HelpActivity.class);
    }

    @NonNull
    public static Intent aiAssistant(@NonNull Context context) {
        return new Intent(context, AiChatActivity.class);
    }
}
