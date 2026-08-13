package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotReplyFactory chatbotReplyFactory;

    public ChatbotResponse ask(String message) {
        UUID conversationId = UUID.randomUUID();
        return new ChatbotResponse(conversationId, chatbotReplyFactory.replyTo(message.trim()));
    }
}
