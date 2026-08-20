package com.ihsanerben.ecommerce_simulation_api.chatbot.messaging;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.chatbot.messaging.ChatbotInteractionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatbotInteractionKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener
    public void publish(ChatbotInteractionEvent event) {
        kafkaTemplate.send(KafkaTopics.CHATBOT_INTERACTION, event.eventId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("event=kafka_publish_failed topic={} eventId={}",
                                KafkaTopics.CHATBOT_INTERACTION, event.eventId(), exception);
                        return;
                    }

                    log.info("event=kafka_published topic={} eventId={} partition={} offset={}",
                            KafkaTopics.CHATBOT_INTERACTION,
                            event.eventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
