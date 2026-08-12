package com.campusgo.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.R;
import com.campusgo.domain.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * C01 消息列表
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SYSTEM = 0;
    private static final int TYPE_TIME = 1;
    private static final int TYPE_MINE = 2;
    private static final int TYPE_OTHER = 3;
    private static final int TYPE_IMAGE = 4;
    private static final int TYPE_VOICE = 5;

    private final List<ChatMessage> items = new ArrayList<>();

    public void setItems(@NonNull List<ChatMessage> messages) {
        items.clear();
        items.addAll(messages);
        notifyDataSetChanged();
    }

    public void append(@NonNull ChatMessage message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = items.get(position);
        switch (msg.type) {
            case SYSTEM:
                return TYPE_SYSTEM;
            case TIME:
                return TYPE_TIME;
            case IMAGE:
                return TYPE_IMAGE;
            case VOICE:
                return TYPE_VOICE;
            case TEXT:
            default:
                return msg.mine ? TYPE_MINE : TYPE_OTHER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_SYSTEM:
                return new SimpleHolder(inflater.inflate(R.layout.item_chat_system, parent, false), R.id.tvSystem);
            case TYPE_TIME:
                return new SimpleHolder(inflater.inflate(R.layout.item_chat_time, parent, false), R.id.tvTime);
            case TYPE_MINE:
                return new MineHolder(inflater.inflate(R.layout.item_chat_mine, parent, false));
            case TYPE_IMAGE:
            case TYPE_VOICE:
            case TYPE_OTHER:
            default:
                return new SimpleHolder(inflater.inflate(R.layout.item_chat_other, parent, false), R.id.tvBubble);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = items.get(position);
        if (holder instanceof SimpleHolder) {
            TextView tv = ((SimpleHolder) holder).textView;
            if (msg.type == ChatMessage.Type.TIME) {
                tv.setText(msg.timeLabel);
            } else if (msg.type == ChatMessage.Type.IMAGE) {
                tv.setText("🖼 " + (msg.content != null ? msg.content : "图片"));
            } else if (msg.type == ChatMessage.Type.VOICE) {
                tv.setText("🎤 语音 " + (msg.voiceDuration != null ? msg.voiceDuration : ""));
            } else {
                tv.setText(msg.content);
            }
        } else if (holder instanceof MineHolder) {
            MineHolder mine = (MineHolder) holder;
            mine.tvBubble.setText(msg.content);
            if (msg.readLabel != null && !msg.readLabel.isEmpty()) {
                mine.tvRead.setVisibility(View.VISIBLE);
                mine.tvRead.setText(msg.readLabel);
            } else {
                mine.tvRead.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static final class SimpleHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        SimpleHolder(@NonNull View itemView, int textId) {
            super(itemView);
            textView = itemView.findViewById(textId);
        }
    }

    private static final class MineHolder extends RecyclerView.ViewHolder {
        final TextView tvBubble;
        final TextView tvRead;

        MineHolder(@NonNull View itemView) {
            super(itemView);
            tvBubble = itemView.findViewById(R.id.tvBubble);
            tvRead = itemView.findViewById(R.id.tvRead);
        }
    }
}
