package com.campusgo.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockChatRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityChatBinding;
import com.campusgo.domain.model.ChatMessage;
import com.campusgo.domain.model.ChatThread;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.task.TaskNavigator;

import java.util.Collections;
import java.util.List;

/**
 * C01 聊天页（支持远程文字消息）
 */
public class ChatActivity extends AppCompatActivity {

    private static final long POLL_INTERVAL_MS = 3000L;

    private ActivityChatBinding binding;
    private ChatMessageAdapter adapter;
    private String threadId;
    private String taskId;
    private UserRole viewerRole;
    private boolean remoteMode;
    private boolean sending;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = this::pollMessages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = ((CampusGoApp) getApplication()).getSessionManager();
        threadId = getIntent().getStringExtra(ChatNavigator.EXTRA_THREAD_ID);
        if (threadId == null) {
            threadId = "c1";
        }
        taskId = getIntent().getStringExtra(ChatNavigator.EXTRA_TASK_ID);
        if (taskId == null) {
            taskId = MockChatRepository.taskIdForThread(threadId);
        }
        String peerName = getIntent().getStringExtra(ChatNavigator.EXTRA_PEER_NAME);
        String taskTitle = getIntent().getStringExtra(ChatNavigator.EXTRA_TASK_TITLE);
        double taskReward = getIntent().getDoubleExtra(ChatNavigator.EXTRA_TASK_REWARD, 15.0);
        viewerRole = parseRole(getIntent().getStringExtra(ChatNavigator.EXTRA_VIEWER_ROLE),
                sessionManager.getActiveRole());
        boolean archived = getIntent().getBooleanExtra(ChatNavigator.EXTRA_ARCHIVED, false);
        boolean systemPeer = peerName != null && peerName.contains("系统")
                || peerName != null && peerName.contains("助手");
        remoteMode = FeatureFlags.USE_REMOTE_API && !systemPeer;

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvPeerName.setText(peerName != null ? peerName : "同学");
        binding.tvPeerStatus.setVisibility(systemPeer ? View.GONE : View.VISIBLE);

        if (taskTitle != null && !taskTitle.isEmpty()) {
            binding.bannerTask.setVisibility(View.VISIBLE);
            binding.tvTaskTitle.setText(taskTitle);
            binding.tvTaskReward.setText(String.format("¥%.2f", taskReward));
            binding.bannerTask.setOnClickListener(v -> openTask());
            binding.btnViewTask.setOnClickListener(v -> openTask());
        } else {
            binding.bannerTask.setVisibility(View.GONE);
            binding.btnViewTask.setVisibility(View.GONE);
        }

        adapter = new ChatMessageAdapter();
        binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMessages.setAdapter(adapter);

        if (archived || systemPeer) {
            binding.inputBar.setVisibility(View.GONE);
            binding.tvArchived.setVisibility(View.VISIBLE);
            if (!remoteMode) {
                adapter.setItems(MockChatRepository.messages(threadId));
                scrollToBottom();
            }
        } else {
            binding.btnSend.setOnClickListener(v -> sendMessage());
            binding.btnVoice.setOnClickListener(v ->
                    Toast.makeText(this, R.string.chat_voice_soon, Toast.LENGTH_SHORT).show());
            binding.btnImage.setOnClickListener(v ->
                    Toast.makeText(this, R.string.chat_image_soon, Toast.LENGTH_SHORT).show());
        }

        if (remoteMode) {
            resolveAndLoadRemote();
        } else if (!archived && !systemPeer) {
            adapter.setItems(MockChatRepository.messages(threadId));
            scrollToBottom();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (remoteMode && isNumericId(threadId)) {
            loadRemoteMessages(false);
            startPolling();
        }
    }

    @Override
    protected void onPause() {
        stopPolling();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopPolling();
        super.onDestroy();
    }

    private void resolveAndLoadRemote() {
        if (isNumericId(threadId)) {
            loadRemoteMessages(true);
            return;
        }
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, R.string.chat_load_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.get().chatRemote().getOrCreateByTask(taskId, new ApiCallback<ChatThread>() {
            @Override
            public void onSuccess(ChatThread data) {
                if (data == null || data.id == null || data.id.isEmpty()) {
                    Toast.makeText(ChatActivity.this, R.string.chat_load_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                threadId = data.id;
                if (data.taskId != null) {
                    taskId = data.taskId;
                }
                if (data.peerName != null && !data.peerName.isEmpty()) {
                    binding.tvPeerName.setText(data.peerName);
                }
                if (data.relatedTask != null && !data.relatedTask.isEmpty()) {
                    binding.bannerTask.setVisibility(View.VISIBLE);
                    binding.tvTaskTitle.setText(data.relatedTask);
                    binding.tvTaskReward.setText(String.format("¥%.2f", data.taskReward));
                    binding.bannerTask.setOnClickListener(v -> openTask());
                    binding.btnViewTask.setOnClickListener(v -> openTask());
                    binding.btnViewTask.setVisibility(View.VISIBLE);
                }
                if (data.archived) {
                    binding.inputBar.setVisibility(View.GONE);
                    binding.tvArchived.setVisibility(View.VISIBLE);
                }
                loadRemoteMessages(true);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                String msg = error.getMessage();
                if (msg == null || msg.isEmpty() || msg.contains("系统内部错误")) {
                    msg = getString(R.string.chat_backend_unavailable);
                }
                Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadRemoteMessages(boolean markRead) {
        if (!isNumericId(threadId)) {
            return;
        }
        RetrofitClient.get().chatRemote().listMessages(threadId, new ApiCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> data) {
                adapter.setItems(data != null ? data : Collections.emptyList());
                scrollToBottom();
                if (markRead) {
                    RetrofitClient.get().chatRemote().markRead(threadId, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                        }

                        @Override
                        public void onError(@NonNull ApiException error) {
                        }
                    });
                }
                startPolling();
            }

            @Override
            public void onError(@NonNull ApiException error) {
                Toast.makeText(ChatActivity.this,
                        error.getMessage() != null ? error.getMessage() : getString(R.string.chat_load_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pollMessages() {
        if (!remoteMode || !isNumericId(threadId) || sending) {
            scheduleNextPoll();
            return;
        }
        RetrofitClient.get().chatRemote().listMessages(threadId, new ApiCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> data) {
                int oldCount = adapter.getItemCount();
                adapter.setItems(data != null ? data : Collections.emptyList());
                if (adapter.getItemCount() > oldCount) {
                    scrollToBottom();
                    RetrofitClient.get().chatRemote().markRead(threadId, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void ignored) {
                        }

                        @Override
                        public void onError(@NonNull ApiException error) {
                        }
                    });
                }
                scheduleNextPoll();
            }

            @Override
            public void onError(@NonNull ApiException error) {
                scheduleNextPoll();
            }
        });
    }

    private void startPolling() {
        stopPolling();
        scheduleNextPoll();
    }

    private void scheduleNextPoll() {
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPolling() {
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text) || sending) {
            return;
        }
        if (remoteMode) {
            if (!isNumericId(threadId)) {
                Toast.makeText(this, R.string.chat_load_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            sending = true;
            binding.btnSend.setEnabled(false);
            RetrofitClient.get().chatRemote().sendText(threadId, text, new ApiCallback<ChatMessage>() {
                @Override
                public void onSuccess(ChatMessage data) {
                    sending = false;
                    binding.btnSend.setEnabled(true);
                    binding.etMessage.setText("");
                    if (data != null) {
                        adapter.append(data);
                        scrollToBottom();
                    } else {
                        loadRemoteMessages(false);
                    }
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    sending = false;
                    binding.btnSend.setEnabled(true);
                    Toast.makeText(ChatActivity.this,
                            error.getMessage() != null ? error.getMessage() : getString(R.string.chat_send_failed),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        ChatMessage message = ChatMessage.text(true, text, null);
        MockChatRepository.append(threadId, message);
        adapter.append(message);
        binding.etMessage.setText("");
        scrollToBottom();
    }

    private void scrollToBottom() {
        binding.recyclerMessages.post(() ->
                binding.recyclerMessages.scrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
    }

    private void openTask() {
        if (taskId == null) {
            return;
        }
        startActivity(TaskNavigator.taskDetail(this, taskId, viewerRole));
    }

    private static boolean isNumericId(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return false;
        }
        for (int i = 0; i < raw.length(); i++) {
            if (!Character.isDigit(raw.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    private UserRole parseRole(@Nullable String raw, @NonNull UserRole fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return UserRole.valueOf(raw);
        } catch (Exception e) {
            return fallback;
        }
    }
}
