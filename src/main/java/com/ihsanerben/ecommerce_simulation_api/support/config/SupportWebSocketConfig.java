package com.ihsanerben.ecommerce_simulation_api.support.config;

import com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys;
import com.ihsanerben.ecommerce_simulation_api.settings.service.ApplicationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class SupportWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ApplicationConfigService applicationConfigService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String allowedOrigin = applicationConfigService.getValue(ApplicationConfigKeys.SECURITY_ALLOWED_ORIGIN);
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigin);
    }
}
