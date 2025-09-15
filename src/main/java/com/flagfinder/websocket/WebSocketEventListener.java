package com.flagfinder.websocket;

import com.flagfinder.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * WebSocket event listener for handling user connection and disconnection events.
 * Manages user online status tracking by listening to WebSocket session lifecycle events.
 * Updates user online status in the database when users connect or disconnect from WebSocket.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserService userService;

    /**
     * Handles WebSocket connection events.
     * Sets the user's online status to true when they establish a WebSocket connection.
     * Extracts the user principal from the session and updates their online status.
     *
     * @param event the session connected event containing connection details
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null && !user.getName().equals("anonymous")) {
            String gameName = user.getName();
            try {
                userService.setUserOnlineStatus(gameName, true);
            } catch (Exception e) {
            }
        } else {
        }
    }

    /**
     * Handles WebSocket disconnection events.
     * Sets the user's online status to false when they disconnect from WebSocket.
     * Extracts the user principal from the session and updates their online status.
     *
     * @param event the session disconnect event containing disconnection details
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null && !user.getName().equals("anonymous")) {
            String gameName = user.getName();
            try {
                userService.setUserOnlineStatus(gameName, false);
            } catch (Exception e) {
            }
        } else {
        }
    }
}
