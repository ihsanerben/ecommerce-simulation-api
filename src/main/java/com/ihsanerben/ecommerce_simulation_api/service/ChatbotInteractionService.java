package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.ChatbotInteraction;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotInteractionEvent;
import com.ihsanerben.ecommerce_simulation_api.repository.ChatbotInteractionRepository;
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
