package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotReply;
import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotResponse;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotInteractionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatbotServiceTest {

    @Test
    void shouldReturnReplyAndPublishInteractionEvent() {
        ChatbotReplyFactory replyFactory = mock(ChatbotReplyFactory.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        when(replyFactory.createReply("Kargom nerede?"))
                .thenReturn(new ChatbotReply("Siparişlerim ekranından takip edebilirsiniz.", "DELIVERY", true));
        ChatbotService chatbotService = new ChatbotService(replyFactory, eventPublisher);

        ChatbotResponse response = chatbotService.ask(" Kargom nerede? ");

        assertThat(response.conversationId()).isNotNull();
        assertThat(response.message()).contains("Siparişlerim");
        verify(eventPublisher).publishEvent(any(ChatbotInteractionEvent.class));
    }
}
