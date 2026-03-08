package com.example.smartjobrecommendation.models;

public class ChatMessage {
    private int chatId;
    private int senderId;
    private int receiverId;
    private String message;
    private long timestamp;
    private int jobId;
    private boolean isRead;
    private String senderName; // For display only

    public ChatMessage() {
    }



    // Getters and Setters
    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
}