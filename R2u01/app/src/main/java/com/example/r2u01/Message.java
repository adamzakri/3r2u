package com.example.r2u01;

public class Message {
    private String senderId;
    private String messageText;
    private long timestamp;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String senderId, String messageText, long timestamp) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
