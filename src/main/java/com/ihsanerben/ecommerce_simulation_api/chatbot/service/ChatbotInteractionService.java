package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.entity.ChatbotInteraction;
import com.ihsanerben.ecommerce_simulation_api.chatbot.messaging.ChatbotInteractionEvent;
import com.ihsanerben.ecommerce_simulation_api.chatbot.repository.ChatbotInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatbotInteractionService {

    private final ChatbotInteractionRepository chatbotInteractionRepository;

    @Transactional
    public void record(ChatbotInteractionEvent event) {
        if (chatbotInteractionRepository.existsByEventId(event.eventId())) {
            return;
        }

        chatbotInteractionRepository.save(ChatbotInteraction.builder()
                .eventId(event.eventId())
                .question(event.question())
                .category(event.category())
                .matched(event.matched())
                .occurredAt(event.occurredAt())
                .recordedAt(Instant.now())
                .build());
    }
}
