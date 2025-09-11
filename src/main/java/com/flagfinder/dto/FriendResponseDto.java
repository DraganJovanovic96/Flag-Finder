package com.flagfinder.dto;

public class FriendResponseDto {
    private String senderUsername;
    private boolean accepted;

    public FriendResponseDto() {}

    public FriendResponseDto(String senderUsername, boolean accepted) {
        this.senderUsername = senderUsername;
        this.accepted = accepted;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
