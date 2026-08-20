package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.entity.ChatbotInteraction;
import com.ihsanerben.ecommerce_simulation_api.chatbot.messaging.ChatbotInteractionEvent;
import com.ihsanerben.ecommerce_simulation_api.chatbot.repository.ChatbotInteractionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatbotInteractionServiceTest {

    private final ChatbotInteractionRepository repository = mock(ChatbotInteractionRepository.class);
    private final ChatbotInteractionService service = new ChatbotInteractionService(repository);

    @Test
    void shouldPersistNewInteraction() {
        ChatbotInteractionEvent event = interactionEvent();

        service.record(event);

        verify(repository).save(any(ChatbotInteraction.class));
    }

    @Test
    void shouldIgnoreAlreadyRecordedEvent() {
        ChatbotInteractionEvent event = interactionEvent();
        when(repository.existsByEventId(event.eventId())).thenReturn(true);

        service.record(event);

        verify(repository, never()).save(any(ChatbotInteraction.class));
    }

    private ChatbotInteractionEvent interactionEvent() {
        return new ChatbotInteractionEvent(
                UUID.randomUUID(),
                "Kargom nerede?",
                "DELIVERY",
                true,
                Instant.now());
    }
}
