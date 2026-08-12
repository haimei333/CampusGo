package com.campusgo.ui.ai;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class AiNavigator {

    @NonNull
    public static Intent chat(@NonNull Context context) {
        return new Intent(context, AiChatActivity.class);
    }

    @NonNull
    public static Intent chat(@NonNull Context context, @NonNull String sessionId) {
        return new Intent(context, AiChatActivity.class)
                .putExtra("sessionId", sessionId);
    }
}
