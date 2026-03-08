package com.example.smartjobrecommendation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatMessage> messageList;
    private int currentUserId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> messageList, int currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderId() == currentUserId) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.messageText.setText(message.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.timeText.setText(time);

        if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            if (holder.senderName != null) {
                holder.senderName.setText(message.getSenderName());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, senderName;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            senderName = itemView.findViewById(R.id.senderName);
        }
    }
}