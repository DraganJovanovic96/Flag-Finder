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

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

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
                } else {
                    log.warn("STOMP token invalid for {}", usernameEmail);
                }
            } else {
                log.warn("STOMP CONNECT missing Authorization header");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String principal = accessor.getUser() != null ? accessor.getUser().getName() : "<none>";
        }

        return message;
    }
}
