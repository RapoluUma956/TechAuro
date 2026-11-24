package com.assetmanagement.backend.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AdminTerminalWebSocketHandler adminHandler;
    private final AgentTerminalWebSocketHandler agentHandler;
    private final AdminRemoteDesktopWebSocketHandler adminDesktopHandler;
    private final AgentRemoteDesktopWebSocketHandler agentDesktopHandler;

    public WebSocketConfig(
            AdminTerminalWebSocketHandler adminHandler,
            AgentTerminalWebSocketHandler agentHandler,
            AdminRemoteDesktopWebSocketHandler adminDesktopHandler,
            AgentRemoteDesktopWebSocketHandler agentDesktopHandler) {
        this.adminHandler = adminHandler;
        this.agentHandler = agentHandler;
        this.adminDesktopHandler = adminDesktopHandler;
        this.agentDesktopHandler = agentDesktopHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Terminal WebSocket Endpoints
        registry.addHandler(adminHandler, "/ws/terminal/admin")
                .setAllowedOriginPatterns("*");

        registry.addHandler(agentHandler, "/ws/terminal/agent")
                .setAllowedOriginPatterns("*");

        // Remote Desktop WebSocket Endpoints (AnyDesk-like live stream & control)
        registry.addHandler(adminDesktopHandler, "/ws/remote-desktop/admin")
                .setAllowedOriginPatterns("*");

        registry.addHandler(agentDesktopHandler, "/ws/remote-desktop/agent")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configure WebSocket container buffer sizes to permanently prevent
     * RFC 6455 Error 1009 ("The decoded text message was too big for the output buffer").
     * Max buffer size set to 10MB for high-res screen frames.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024); // 10 MB
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10 MB
        container.setMaxSessionIdleTimeout(300000L); // 5 minutes
        return container;
    }
}
