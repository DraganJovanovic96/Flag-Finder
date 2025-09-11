package com.flagfinder.dto;

import java.time.LocalDateTime;

public class FriendshipDto {
    private String username;
    private LocalDateTime createdAt;

    public FriendshipDto() {}

    public FriendshipDto(String username, LocalDateTime createdAt) {
        this.username = username;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
