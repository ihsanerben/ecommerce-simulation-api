package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotResponse;
import com.ihsanerben.ecommerce_simulation_api.chatbot.exception.ChatbotUnavailableException;
import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotRequestEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotResponseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final long RESPONSE_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConcurrentMap<UUID, CompletableFuture<ChatbotResponseEvent>> pendingResponses =
            new ConcurrentHashMap<>();

    public ChatbotResponse ask(String message) {
        UUID conversationId = UUID.randomUUID();
        CompletableFuture<ChatbotResponseEvent> pendingResponse = new CompletableFuture<>();
        pendingResponses.put(conversationId, pendingResponse);

        ChatbotRequestEvent event = new ChatbotRequestEvent(
                conversationId,
                message.trim(),
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.CHATBOT_REQUEST, conversationId.toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        pendingResponse.completeExceptionally(exception);
                    }
                });

        try {
            ChatbotResponseEvent response = pendingResponse.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return new ChatbotResponse(response.conversationId(), response.message());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ChatbotUnavailableException(
                    "Chatbot is temporarily unavailable. Please try again later.",
                    exception
            );
        } catch (ExecutionException | TimeoutException exception) {
            throw new ChatbotUnavailableException(
                    "Chatbot is temporarily unavailable. Please try again later.",
                    exception
            );
        } finally {
            pendingResponses.remove(conversationId);
        }
    }

    public void complete(ChatbotResponseEvent event) {
        CompletableFuture<ChatbotResponseEvent> pendingResponse = pendingResponses.get(event.conversationId());
        if (pendingResponse != null) {
            pendingResponse.complete(event);
        }
    }
}
