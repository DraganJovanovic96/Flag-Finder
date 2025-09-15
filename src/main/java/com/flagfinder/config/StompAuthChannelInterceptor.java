package com.flagfinder.config;

import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * STOMP channel interceptor for WebSocket authentication.
 * Intercepts CONNECT commands to validate JWT tokens and set user authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    /**
     * Service for JWT token operations.
     */
    private final JwtService jwtService;
    
    /**
     * Service for loading user details during authentication.
     */
    private final UserDetailsService userDetailsService;
    
    /**
     * Repository for user data access.
     */
    private final UserRepository userRepository;

    /**
     * Intercepts messages before they are sent to authenticate WebSocket connections.
     * Validates JWT tokens from Authorization header during CONNECT commands.
     *
     * @param message the message being sent
     * @param channel the message channel
     * @return the message (potentially modified)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String bearerToken = (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;

            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7);
                String usernameEmail = jwtService.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    var userOpt = userRepository.findByEmail(usernameEmail);
                    String principalName = userOpt.map(com.flagfinder.model.User::getGameName).orElse(usernameEmail);
                    accessor.setUser(() -> principalName);
                }
            }
        }

        return message;
    }
}
