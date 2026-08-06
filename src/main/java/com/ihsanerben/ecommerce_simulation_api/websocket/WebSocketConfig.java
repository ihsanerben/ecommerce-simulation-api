package com.ihsanerben.ecommerce_simulation_api.websocket;

import com.ihsanerben.ecommerce_simulation_api.service.ApplicationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.SECURITY_ALLOWED_ORIGIN;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveNotificationHandler liveNotificationHandler;
    private final ApplicationConfigService applicationConfigService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveNotificationHandler, "/ws/notifications")
                .setAllowedOrigins(applicationConfigService.getValue(SECURITY_ALLOWED_ORIGIN));
    }
}
