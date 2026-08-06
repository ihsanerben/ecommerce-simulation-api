package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreatedEventConsumer {

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED)
    public void consume(OrderCreatedEvent event) {
        log.info("event=order_created_consumed eventId={} orderId={} userId={} itemCount={}",
                event.eventId(), event.orderId(), event.userId(), event.itemCount());
    }
}
