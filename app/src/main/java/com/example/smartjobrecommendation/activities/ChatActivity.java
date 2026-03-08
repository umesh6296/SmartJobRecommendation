package com.example.smartjobrecommendation.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.adapters.ChatAdapter;
import com.example.smartjobrecommendation.database.DatabaseHelper;
import com.example.smartjobrecommendation.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatWithText;

    private DatabaseHelper db;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    private int currentUserId;
    private int otherUserId;
    private String otherUserName;
    private int jobId;

    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get intent data
        currentUserId = getIntent().getIntExtra("currentUserId", -1);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        otherUserName = getIntent().getStringExtra("otherUserName");
        jobId = getIntent().getIntExtra("jobId", -1);

        if (currentUserId == -1 || otherUserId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new DatabaseHelper(this);
        initViews();
        setupChat();
        loadMessages();

        // Auto-refresh every 2 seconds
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(refreshRunnable);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatWithText = findViewById(R.id.chatWithText);

        chatWithText.setText("Chat with: " + otherUserName);

        adapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupChat() {
        // Mark previous messages as read
        db.markMessagesAsRead(currentUserId, otherUserId, jobId);
    }

    private void loadMessages() {
        Cursor cursor = db.getChatHistory(currentUserId, otherUserId, jobId);

        messageList.clear();

        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setChatId(cursor.getInt(cursor.getColumnIndexOrThrow("chatId")));
                message.setSenderId(cursor.getInt(cursor.getColumnIndexOrThrow("senderId")));
                message.setReceiverId(cursor.getInt(cursor.getColumnIndexOrThrow("receiverId")));
                message.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                message.setJobId(cursor.getInt(cursor.getColumnIndexOrThrow("jobId")));
                message.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1);

                // Get sender name
                if (message.getSenderId() == otherUserId) {
                    message.setSenderName(otherUserName);
                } else {
                    message.setSenderName("You");
                }

                messageList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();

        // Scroll to bottom
        if (messageList.size() > 0) {
            recyclerView.scrollToPosition(messageList.size() - 1);
        }

        // Mark new messages as read
        db.markMessagesAsRead(currentUserId, otherUserId, jobId);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        boolean sent = db.sendMessage(currentUserId, otherUserId, messageText, jobId);

        if (sent) {
            messageInput.setText("");
            loadMessages(); // Instant load
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}