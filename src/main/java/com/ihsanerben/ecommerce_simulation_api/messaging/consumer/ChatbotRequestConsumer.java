package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.chatbot.service.ChatbotReplyFactory;
import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotRequestEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.ChatbotResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatbotRequestConsumer {

    private final ChatbotReplyFactory chatbotReplyFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.CHATBOT_REQUEST, groupId = "chatbot-worker")
    public void consume(ChatbotRequestEvent request) {
        log.info("event=chatbot_request_consumed conversationId={}", request.conversationId());
        ChatbotResponseEvent response = new ChatbotResponseEvent(
                request.conversationId(),
                chatbotReplyFactory.replyTo(request.message()),
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.CHATBOT_RESPONSE, request.conversationId().toString(), response);
    }
}
