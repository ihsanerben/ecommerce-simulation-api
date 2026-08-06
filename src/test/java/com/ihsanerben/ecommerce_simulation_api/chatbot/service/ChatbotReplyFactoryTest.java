package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotReplyFactoryTest {

    private final ChatbotReplyFactory chatbotReplyFactory = new ChatbotReplyFactory();

    @Test
    void shouldAnswerDeliveryQuestions() {
        String response = chatbotReplyFactory.replyTo("Kargom ne zaman gelir?");

        assertThat(response).contains("Siparişlerim");
    }

    @Test
    void shouldReturnGuidanceForUnknownQuestions() {
        String response = chatbotReplyFactory.replyTo("Bana bir şiir yazar mısın?");

        assertThat(response).contains("henüz ayrıntılı yanıt veremiyorum");
    }
}
