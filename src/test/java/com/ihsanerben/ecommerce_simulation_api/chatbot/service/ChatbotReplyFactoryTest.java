package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotReply;
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

    @Test
    void shouldClassifyMatchedQuestion() {
        ChatbotReply reply = chatbotReplyFactory.createReply("Sepete ürün eklemek istiyorum");

        assertThat(reply.category()).isEqualTo("CART");
        assertThat(reply.matched()).isTrue();
    }

    @Test
    void shouldClassifyUnknownQuestionAsUnmatched() {
        ChatbotReply reply = chatbotReplyFactory.createReply("Bana bir şiir yazar mısın?");

        assertThat(reply.category()).isEqualTo("UNKNOWN");
        assertThat(reply.matched()).isFalse();
    }
}
