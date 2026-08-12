package com.campusgo.ui.tasks;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.R;
import com.campusgo.databinding.ItemTaskM02Binding;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(@NonNull TaskListItem item);
    }

    private final List<TaskListItem> items = new ArrayList<>();
    private OnTaskClickListener listener;

    public void setItems(@NonNull List<TaskListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskM02Binding binding = ItemTaskM02Binding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskListItem item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskM02Binding binding;

        ViewHolder(ItemTaskM02Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull TaskListItem item) {
            binding.tvTitle.setText(item.title);
            binding.tvStatusTag.setText(item.statusLabel);
            binding.tvDesc.setText(item.description);
            if (item.priceLabel == null) {
                binding.tvPrice.setText("—");
            } else {
                binding.tvPrice.setText(item.priceLabel);
            }
            applyTagStyle(item);
        }

        private void applyTagStyle(@NonNull TaskListItem item) {
            int bg;
            int textColor;
            if (item.status == TaskStatus.DRAFT) {
                bg = R.drawable.bg_tag_draft;
                textColor = R.color.cg_text_tertiary;
            } else if (item.status == TaskStatus.COMPLETED || item.status == TaskStatus.REVIEWED) {
                bg = R.drawable.bg_tag_group;
                textColor = R.color.cg_tag_group_text;
            } else if (item.mode == TaskMode.GROUP || "拼单中".equals(item.statusLabel)
                    || item.statusLabel.contains("拼单") || "差1人".equals(item.statusLabel)) {
                bg = R.drawable.bg_tag_group;
                textColor = R.color.cg_tag_group_text;
            } else if (item.mode == TaskMode.EMERGENCY || "紧急".equals(item.statusLabel)) {
                bg = R.drawable.bg_tag_emergency;
                textColor = R.color.cg_tag_emergency_text;
            } else if (item.mode == TaskMode.RESERVE || item.statusLabel.contains("预约")
                    || "已占位".equals(item.statusLabel)) {
                bg = R.drawable.bg_tag_reserve;
                textColor = R.color.cg_tag_reserve_text;
            } else {
                bg = R.drawable.bg_tag;
                textColor = R.color.cg_tag_normal_text;
            }
            binding.tvStatusTag.setBackgroundResource(bg);
            binding.tvStatusTag.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), textColor));
        }
    }
}
