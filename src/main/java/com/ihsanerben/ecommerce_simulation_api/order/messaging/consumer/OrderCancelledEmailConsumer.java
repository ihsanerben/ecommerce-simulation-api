package com.ihsanerben.ecommerce_simulation_api.order.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.order.messaging.event.OrderCancelledEvent;
import com.ihsanerben.ecommerce_simulation_api.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledEmailConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "order-cancelled-email")
    public void consume(OrderCancelledEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("event=order_cancelled_email_skipped reason=missing_recipient eventId={} orderId={}",
                    event.eventId(), event.orderId());
            return;
        }

        emailService.sendOrderCancelled(event.recipientEmail(), event.orderId());
        log.info("event=order_cancelled_email_sent eventId={} orderId={}",
                event.eventId(), event.orderId());
    }
}
