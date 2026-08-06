package com.ihsanerben.ecommerce_simulation_api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveNotificationService {

    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 64 * 1024;

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(
                session,
                SEND_TIME_LIMIT_MS,
                BUFFER_SIZE_LIMIT_BYTES
        ));
        log.info("event=websocket_connected sessionId={} activeConnections={}", session.getId(), sessions.size());
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session.getId());
        log.info("event=websocket_disconnected sessionId={} activeConnections={}", session.getId(), sessions.size());
    }

    public void broadcastOrderCreated() {
        LiveNotification notification = new LiveNotification(
                "ORDER_CREATED",
                "Yeni bir sipariş oluşturuldu.",
                Instant.now()
        );
        broadcast(notification);
    }

    private void broadcast(LiveNotification notification) {
        TextMessage message;
        try {
            message = new TextMessage(objectMapper.writeValueAsString(notification));
        } catch (JsonProcessingException exception) {
            log.error("event=websocket_message_serialization_failed type={}", notification.type(), exception);
            return;
        }

        sessions.forEach((sessionId, session) -> send(sessionId, session, message));
    }

    private void send(String sessionId, WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            sessions.remove(sessionId);
            return;
        }
        try {
            session.sendMessage(message);
        } catch (IOException exception) {
            sessions.remove(sessionId);
            log.warn("event=websocket_message_send_failed sessionId={}", sessionId, exception);
        }
    }
}
