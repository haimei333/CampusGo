package com.campusgo.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockChatRepository;
import com.campusgo.data.mock.MockMessageRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.FragmentMessagesBinding;
import com.campusgo.domain.model.AppNotification;
import com.campusgo.domain.model.ChatThread;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.chat.ChatNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * M03 消息：聊天 / 通知双 Tab
 */
public class MessageFragment extends Fragment {

    private enum MessageTab { CHAT, NOTIFY }

    private FragmentMessagesBinding binding;
    private SessionManager sessionManager;
    private MessageTab activeTab = MessageTab.CHAT;
    private ChatListAdapter chatAdapter;
    private NotificationListAdapter notifyAdapter;
    private final List<ChatThread> remoteChats = new ArrayList<>();
    private final List<AppNotification> remoteNotifications = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = ((CampusGoApp) requireActivity().getApplication()).getSessionManager();

        chatAdapter = new ChatListAdapter();
        chatAdapter.setOnChatClickListener(thread -> {
            boolean archived = thread.archived || thread.peerRole == null;
            String taskId = thread.taskId != null
                    ? thread.taskId
                    : MockChatRepository.taskIdForThread(thread.id);
            startActivity(ChatNavigator.open(
                    requireContext(),
                    thread.id,
                    thread.peerName,
                    taskId,
                    thread.relatedTask,
                    thread.taskReward > 0 ? thread.taskReward : 15.0,
                    sessionManager.getActiveRole(),
                    archived));
        });

        notifyAdapter = new NotificationListAdapter();
        notifyAdapter.setOnNotificationClickListener(notification -> {
            notification.unread = false;
            notifyAdapter.notifyDataSetChanged();
            updateTabLabels(sessionManager.getActiveRole());
            if (FeatureFlags.USE_REMOTE_API) {
                RetrofitClient.get().notificationRemote().markRead(notification.id, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                    }
                });
            }
            NotificationNavigator.open(requireContext(), notification, sessionManager);
        });

        binding.recyclerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View child,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(child);
                if (pos > 0) {
                    outRect.top = 1;
                }
            }
        });

        binding.tabChat.setOnClickListener(v -> selectTab(MessageTab.CHAT));
        binding.tabNotify.setOnClickListener(v -> selectTab(MessageTab.NOTIFY));
        binding.btnMarkAllRead.setOnClickListener(v -> {
            if (FeatureFlags.USE_REMOTE_API) {
                RetrofitClient.get().notificationRemote().markAllRead(new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        loadRemoteNotifications();
                        Toast.makeText(requireContext(), R.string.messages_marked_read, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        Toast.makeText(requireContext(), R.string.chat_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                MockMessageRepository.markAllNotificationsRead();
                notifyAdapter.setItems(MockMessageRepository.notifications());
                updateTabLabels(sessionManager.getActiveRole());
                Toast.makeText(requireContext(), R.string.messages_marked_read, Toast.LENGTH_SHORT).show();
            }
        });

        selectTab(MessageTab.CHAT);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void selectTab(@NonNull MessageTab tab) {
        activeTab = tab;
        styleTab(binding.tabChat, tab == MessageTab.CHAT);
        styleTab(binding.tabNotify, tab == MessageTab.NOTIFY);
        binding.btnMarkAllRead.setVisibility(tab == MessageTab.NOTIFY ? View.VISIBLE : View.GONE);
        refreshList();
    }

    private void styleTab(@NonNull TextView tab, boolean selected) {
        tab.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.cg_brand : R.color.cg_text_tertiary));
        tab.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : android.R.color.transparent);
    }

    private void refreshList() {
        UserRole role = sessionManager.getActiveRole();
        updateTabLabels(role);
        if (activeTab == MessageTab.CHAT) {
            binding.recyclerList.setAdapter(chatAdapter);
            if (FeatureFlags.USE_REMOTE_API) {
                loadRemoteChats();
            } else {
                chatAdapter.setItems(MockMessageRepository.chats(role));
                updateEmptyState();
            }
        } else {
            binding.recyclerList.setAdapter(notifyAdapter);
            if (FeatureFlags.USE_REMOTE_API) {
                loadRemoteNotifications();
            } else {
                notifyAdapter.setItems(MockMessageRepository.notifications());
                updateEmptyState();
            }
        }
    }

    private void loadRemoteNotifications() {
        RetrofitClient.get().notificationRemote().list(new ApiCallback<List<AppNotification>>() {
            @Override
            public void onSuccess(List<AppNotification> data) {
                if (binding == null || activeTab != MessageTab.NOTIFY) {
                    return;
                }
                remoteNotifications.clear();
                if (data != null) {
                    remoteNotifications.addAll(data);
                }
                notifyAdapter.setItems(new ArrayList<>(remoteNotifications));
                updateTabLabels(sessionManager.getActiveRole());
                updateEmptyState();
            }

            @Override
            public void onError(@NonNull ApiException error) {
                if (binding == null || activeTab != MessageTab.NOTIFY) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.chat_load_failed, Toast.LENGTH_SHORT).show();
                notifyAdapter.setItems(new ArrayList<>());
                updateEmptyState();
            }
        });
    }

    private void loadRemoteChats() {
        RetrofitClient.get().chatRemote().listConversations(new ApiCallback<List<ChatThread>>() {
            @Override
            public void onSuccess(List<ChatThread> data) {
                if (binding == null || activeTab != MessageTab.CHAT) {
                    return;
                }
                remoteChats.clear();
                if (data != null) {
                    remoteChats.addAll(data);
                }
                chatAdapter.setItems(new ArrayList<>(remoteChats));
                updateTabLabels(sessionManager.getActiveRole());
                updateEmptyState();
            }

            @Override
            public void onError(@NonNull ApiException error) {
                if (binding == null || activeTab != MessageTab.CHAT) {
                    return;
                }
                Toast.makeText(requireContext(),
                        error.getMessage() != null ? error.getMessage() : getString(R.string.chat_load_failed),
                        Toast.LENGTH_SHORT).show();
                chatAdapter.setItems(new ArrayList<>());
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = binding.recyclerList.getAdapter() != null
                && binding.recyclerList.getAdapter().getItemCount() == 0;
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateTabLabels(@NonNull UserRole role) {
        int chatUnread;
        if (FeatureFlags.USE_REMOTE_API) {
            chatUnread = 0;
            for (ChatThread c : remoteChats) {
                chatUnread += c.unreadCount;
            }
        } else {
            chatUnread = MockMessageRepository.chatUnreadTotal(role);
        }
        int notifyUnread;
        if (FeatureFlags.USE_REMOTE_API) {
            notifyUnread = 0;
            for (AppNotification n : remoteNotifications) {
                if (n.unread) {
                    notifyUnread++;
                }
            }
        } else {
            notifyUnread = MockMessageRepository.notifyUnreadTotal();
        }
        binding.tabChat.setText(chatUnread > 0
                ? getString(R.string.messages_tab_chat) + " (" + chatUnread + ")"
                : getString(R.string.messages_tab_chat));
        binding.tabNotify.setText(notifyUnread > 0
                ? getString(R.string.messages_tab_notify) + " (" + notifyUnread + ")"
                : getString(R.string.messages_tab_notify));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
