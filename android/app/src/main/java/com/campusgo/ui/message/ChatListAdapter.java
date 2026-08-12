package com.campusgo.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.R;
import com.campusgo.databinding.ItemMessageChatBinding;
import com.campusgo.domain.model.ChatThread;
import com.campusgo.domain.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(@NonNull ChatThread thread);
    }

    private final List<ChatThread> items = new ArrayList<>();
    private OnChatClickListener listener;

    public void setItems(@NonNull List<ChatThread> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageChatBinding binding = ItemMessageChatBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatThread item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageChatBinding binding;

        ViewHolder(ItemMessageChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull ChatThread item) {
            String initial = item.peerName.isEmpty() ? "?" : item.peerName.substring(0, 1);
            binding.tvAvatar.setText(initial);
            binding.tvName.setText(item.peerName);
            bindPeerRole(item.peerRole);
            binding.tvTime.setText(item.timeLabel);
            binding.tvPreview.setText(item.preview);
            if (item.relatedTask != null && !item.relatedTask.isEmpty()) {
                binding.tvRelatedTask.setVisibility(View.VISIBLE);
                binding.tvRelatedTask.setText(item.relatedTask);
            } else {
                binding.tvRelatedTask.setVisibility(View.GONE);
            }
            if (item.unreadCount > 0) {
                binding.tvUnread.setVisibility(View.VISIBLE);
                binding.tvUnread.setText(String.valueOf(item.unreadCount));
            } else {
                binding.tvUnread.setVisibility(View.GONE);
            }
        }

        private void bindPeerRole(@Nullable UserRole peerRole) {
            if (peerRole == null) {
                binding.tvPeerRole.setVisibility(View.GONE);
                return;
            }
            binding.tvPeerRole.setVisibility(View.VISIBLE);
            if (peerRole == UserRole.RUNNER) {
                binding.tvPeerRole.setText(R.string.chat_peer_runner);
                binding.tvPeerRole.setBackgroundResource(R.drawable.bg_tag_group);
                binding.tvPeerRole.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.cg_tag_group_text));
            } else {
                binding.tvPeerRole.setText(R.string.chat_peer_publisher);
                binding.tvPeerRole.setBackgroundResource(R.drawable.bg_tag_reserve);
                binding.tvPeerRole.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.cg_tag_reserve_text));
            }
        }
    }
}
