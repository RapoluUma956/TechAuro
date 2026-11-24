package com.assetmanagement.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AdminTerminalWebSocketHandler adminHandler;
    private final AgentTerminalWebSocketHandler agentHandler;

    public WebSocketConfig(AdminTerminalWebSocketHandler adminHandler, AgentTerminalWebSocketHandler agentHandler) {
        this.adminHandler = adminHandler;
        this.agentHandler = agentHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminHandler, "/ws/terminal/admin")
                .setAllowedOriginPatterns("*");

        registry.addHandler(agentHandler, "/ws/terminal/agent")
                .setAllowedOriginPatterns("*");
    }
}

