package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotInteractionEvent;
import com.ihsanerben.ecommerce_simulation_api.service.ChatbotInteractionService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatbotInteractionConsumerTest {

    @Test
    void shouldRecordConsumedInteraction() {
        ChatbotInteractionService service = mock(ChatbotInteractionService.class);
        ChatbotInteractionEvent event = new ChatbotInteractionEvent(
                UUID.randomUUID(),
                "Kargom nerede?",
                "DELIVERY",
                true,
                Instant.now());

        new ChatbotInteractionConsumer(service).consume(event);

        verify(service).record(event);
    }
}
