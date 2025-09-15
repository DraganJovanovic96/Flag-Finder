package com.flagfinder.enumeration;

/**
 * Enumeration representing the status of a game room.
 */
public enum RoomStatus {
    /**
     * Room is waiting for a guest player to join.
     */
    WAITING_FOR_GUEST,
    
    /**
     * Room has all players and is ready to start the game.
     */
    ROOM_READY_FOR_START,
    
    /**
     * Game is currently in progress in the room.
     */
    GAME_IN_PROGRESS,
    
    /**
     * Game in the room has been completed.
     */
    GAME_COMPLETED
}

