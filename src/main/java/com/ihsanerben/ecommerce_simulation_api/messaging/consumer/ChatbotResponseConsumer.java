package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.chatbot.service.ChatbotService;
import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatbotResponseConsumer {

    private final ChatbotService chatbotService;

    @KafkaListener(topics = KafkaTopics.CHATBOT_RESPONSE, groupId = "chatbot-api")
    public void consume(ChatbotResponseEvent response) {
        chatbotService.complete(response);
        log.info("event=chatbot_response_consumed conversationId={}", response.conversationId());
    }
}
