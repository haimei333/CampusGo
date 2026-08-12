package com.campusgo.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.AiRemoteDataSource;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.ai.HistoryResponse;
import com.campusgo.databinding.ActivityAiChatBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AiChatActivity extends AppCompatActivity {

    private static final String TAG = "AiChatActivity";
    private ActivityAiChatBinding binding;
    private AiMessageAdapter adapter;
    private AiRemoteDataSource aiRemoteDataSource;
    private SessionManager sessionManager;
    private String currentSessionId;
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();
        aiRemoteDataSource = new AiRemoteDataSource(
                RetrofitClient.get().aiApi(),
                sessionManager);

        currentSessionId = getIntent().getStringExtra("sessionId");
        if (currentSessionId == null) {
            currentSessionId = UUID.randomUUID().toString();
        }

        setupUI();
        loadHistory();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnClear.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.ai_chat_clear_confirm_title)
                    .setMessage(R.string.ai_chat_clear_confirm_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> clearSession())
                    .setNegativeButton(R.string.back, null)
                    .show();
        });

        adapter = new AiMessageAdapter();
        binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());

        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        binding.btnSend.setEnabled(!loading);
        binding.btnSend.setAlpha(loading ? 0.5f : 1.0f);
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadHistory() {
        Log.d(TAG, "Loading history for session: " + currentSessionId);
        aiRemoteDataSource.getHistory(currentSessionId, new AiRemoteDataSource.HistoryCallback() {
            @Override
            public void onSuccess(List<HistoryResponse.Message> messages) {
                Log.d(TAG, "History loaded: " + (messages != null ? messages.size() : 0) + " messages");
                runOnUiThread(() -> {
                    if (binding == null) return;
                    List<AiMessageAdapter.AiMessage> aiMessages = new ArrayList<>();
                    if (messages != null) {
                        for (HistoryResponse.Message msg : messages) {
                            aiMessages.add(new AiMessageAdapter.AiMessage(msg.getRole(), msg.getContent()));
                        }
                    }
                    adapter.setMessages(aiMessages);
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Load history error: " + error);
                runOnUiThread(() -> {
                    if (binding == null) return;
                    Toast.makeText(AiChatActivity.this, "加载历史失败: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendMessage() {
        String message = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message) || isLoading) {
            return;
        }

        binding.etMessage.setText("");
        adapter.addMessage(new AiMessageAdapter.AiMessage("user", message));
        scrollToBottom();

        setLoading(true);
        Log.d(TAG, "Sending message: " + message + ", session: " + currentSessionId);

        aiRemoteDataSource.chat(currentSessionId, message, new AiRemoteDataSource.ChatCallback() {
            @Override
            public void onSuccess(String sessionId, String reply) {
                Log.d(TAG, "Reply received: " + reply);
                runOnUiThread(() -> {
                    if (binding == null) return;
                    currentSessionId = sessionId;
                    setLoading(false);
                    adapter.addMessage(new AiMessageAdapter.AiMessage("assistant", reply));
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Chat error: " + error);
                runOnUiThread(() -> {
                    if (binding == null) return;
                    setLoading(false);
                    String errorMsg = "抱歉，我遇到了一些问题：" + error;
                    adapter.addMessage(new AiMessageAdapter.AiMessage("assistant", errorMsg));
                    scrollToBottom();
                    Toast.makeText(AiChatActivity.this, "发送失败: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void clearSession() {
        aiRemoteDataSource.clearSession(currentSessionId, new AiRemoteDataSource.ClearCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (binding == null) return;
                    adapter.setMessages(new ArrayList<>());
                    currentSessionId = UUID.randomUUID().toString();
                    Toast.makeText(AiChatActivity.this, R.string.ai_chat_clear_success, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (binding == null) return;
                    Toast.makeText(AiChatActivity.this, R.string.ai_chat_clear_failed + ": " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            binding.recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}
