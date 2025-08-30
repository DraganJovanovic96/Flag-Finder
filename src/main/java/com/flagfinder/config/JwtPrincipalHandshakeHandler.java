package com.flagfinder.config;

import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
                                      @NonNull WebSocketHandler wsHandler,
                                      @NonNull Map<String, Object> attributes) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        String token = null;
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearer = authHeaders.get(0);
            if (bearer != null && bearer.startsWith("Bearer ")) {
                token = bearer.substring(7);
            }
        }

        if (token == null && request.getURI().getQuery() != null) {
            String[] params = request.getURI().getQuery().split("&");
            for (String p : params) {
                int idx = p.indexOf('=');
                if (idx > 0) {
                    String key = p.substring(0, idx);
                    String val = p.substring(idx + 1);
                    if ("token".equals(key)) {
                        token = val;
                        break;
                    }
                }
            }
        }

        if (token != null) {
            try {
                String email = jwtService.extractUsername(token);
                String gameName = userRepository.findByEmail(email)
                        .map(u -> u.getGameName())
                        .orElse(email);
                return () -> gameName;
            } catch (Exception e) {
                log.warn("Failed to resolve principal from JWT in handshake: {}", e.getMessage());
            }
        }

        log.warn("No JWT on WS handshake; anonymous principal assigned");
        return () -> "anonymous";
    }
}
