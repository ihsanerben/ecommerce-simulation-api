package com.ihsanerben.ecommerce_simulation_api.messaging.producer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event.orderId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("event=kafka_publish_failed topic={} eventId={} orderId={}",
                                KafkaTopics.ORDER_CREATED, event.eventId(), event.orderId(), exception);
                        return;
                    }

                    log.info("event=kafka_published topic={} eventId={} orderId={} partition={} offset={}",
                            KafkaTopics.ORDER_CREATED,
                            event.eventId(),
                            event.orderId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
