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

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null && !user.getName().equals("anonymous")) {
            String gameName = user.getName();
            log.info("User connected: {}", gameName);
            try {
                userService.setUserOnlineStatus(gameName, true);
            } catch (Exception e) {
                log.warn("Failed to set online status for user {}: {}", gameName, e.getMessage());
            }
        } else {
            log.debug("Anonymous user connected, skipping online status update");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null && !user.getName().equals("anonymous")) {
            String gameName = user.getName();
            log.info("User disconnected: {}", gameName);
            try {
                userService.setUserOnlineStatus(gameName, false);
            } catch (Exception e) {
                log.warn("Failed to set offline status for user {}: {}", gameName, e.getMessage());
            }
        } else {
            log.debug("Anonymous user disconnected, skipping online status update");
        }
    }
}
