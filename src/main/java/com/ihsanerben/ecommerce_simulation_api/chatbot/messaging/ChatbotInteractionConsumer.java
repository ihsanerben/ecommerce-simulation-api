package com.ihsanerben.ecommerce_simulation_api.chatbot.messaging;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.chatbot.messaging.ChatbotInteractionEvent;
import com.ihsanerben.ecommerce_simulation_api.chatbot.service.ChatbotInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatbotInteractionConsumer {

    private final ChatbotInteractionService chatbotInteractionService;

    @KafkaListener(topics = KafkaTopics.CHATBOT_INTERACTION, groupId = "chatbot-interaction-statistics")
    public void consume(ChatbotInteractionEvent event) {
        chatbotInteractionService.record(event);
        log.info("event=chatbot_interaction_recorded eventId={} category={} matched={}",
                event.eventId(), event.category(), event.matched());
    }
}
