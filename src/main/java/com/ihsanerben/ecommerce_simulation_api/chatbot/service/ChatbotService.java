package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotResponse;
import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotReply;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotReplyFactory chatbotReplyFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ChatbotResponse ask(String message) {
        UUID conversationId = UUID.randomUUID();
        String question = message.trim();
        ChatbotReply reply = chatbotReplyFactory.createReply(question);

        applicationEventPublisher.publishEvent(new ChatbotInteractionEvent(
                conversationId,
                question,
                reply.category(),
                reply.matched(),
                Instant.now()));

        return new ChatbotResponse(conversationId, reply.message());
    }
}
