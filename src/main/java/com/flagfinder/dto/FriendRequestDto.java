package com.flagfinder.dto;

import java.time.LocalDateTime;

public class FriendRequestDto {
    private String senderUsername;
    private String targetUsername;
    private String message;
    private LocalDateTime createdAt;

    public FriendRequestDto() {}

    public FriendRequestDto(String senderUsername, String targetUsername, String message, LocalDateTime createdAt) {
        this.senderUsername = senderUsername;
        this.targetUsername = targetUsername;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
