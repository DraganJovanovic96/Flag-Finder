package com.flagfinder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration class that sets up WebSocket message broker and endpoints.
 * Configures STOMP endpoints, message brokers, and authentication for real-time communication.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Value("${spring.frontend.url}")
    private String frontendUrl;
    private final JwtPrincipalHandshakeHandler jwtPrincipalHandshakeHandler;

    /**
     * Configures the message broker for WebSocket communication.
     * Sets up simple broker with topic and queue destinations.
     *
     * @param config the MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     * Sets up endpoints with SockJS fallback and JWT authentication.
     *
     * @param registry the StompEndpointRegistry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl)
                .setHandshakeHandler(jwtPrincipalHandshakeHandler)
                .withSockJS();

        registry.addEndpoint("/ws-native")
                .setAllowedOrigins(frontendUrl)
                .setHandshakeHandler(jwtPrincipalHandshakeHandler);
    }

    /**
     * Configures client inbound channel with authentication interceptor.
     *
     * @param registration the ChannelRegistration to configure
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
