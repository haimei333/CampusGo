package com.campusgo.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.databinding.ItemAiMessageUserBinding;
import com.campusgo.databinding.ItemAiMessageAssistantBinding;

import java.util.ArrayList;
import java.util.List;

public class AiMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private final List<AiMessage> messages = new ArrayList<>();

    public static class AiMessage {
        private final String role;
        private final String content;

        public AiMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    @Override
    public int getItemViewType(int position) {
        AiMessage message = messages.get(position);
        if ("user".equals(message.getRole())) {
            return VIEW_TYPE_USER;
        }
        return VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            ItemAiMessageUserBinding binding = ItemAiMessageUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(binding);
        } else {
            ItemAiMessageAssistantBinding binding = ItemAiMessageAssistantBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new AssistantViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<AiMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(AiMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAiMessageUserBinding binding;

        public UserViewHolder(@NonNull ItemAiMessageUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AiMessage message) {
            binding.tvMessage.setText(message.getContent());
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final ItemAiMessageAssistantBinding binding;

        public AssistantViewHolder(@NonNull ItemAiMessageAssistantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AiMessage message) {
            binding.tvMessage.setText(message.getContent());
        }
    }
}
