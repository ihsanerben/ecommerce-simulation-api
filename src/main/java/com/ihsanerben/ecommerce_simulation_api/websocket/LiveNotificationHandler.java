package com.ihsanerben.ecommerce_simulation_api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class LiveNotificationHandler extends TextWebSocketHandler {

    private final LiveNotificationService liveNotificationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        liveNotificationService.register(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        liveNotificationService.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        liveNotificationService.unregister(session);
    }
}
