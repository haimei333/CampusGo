package com.campusgo.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.databinding.ItemMessageNotificationBinding;
import com.campusgo.domain.model.AppNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(@NonNull AppNotification notification);
    }

    private final List<AppNotification> items = new ArrayList<>();
    private OnNotificationClickListener listener;

    public void setItems(@NonNull List<AppNotification> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageNotificationBinding binding = ItemMessageNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageNotificationBinding binding;

        ViewHolder(ItemMessageNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull AppNotification item) {
            binding.tvTitle.setText(item.title);
            binding.tvBody.setText(item.body);
            binding.tvTime.setText(item.timeLabel);
            binding.dotUnread.setVisibility(item.unread ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
