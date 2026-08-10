package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmationEmailConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-confirmation-email")
    public void consume(OrderCreatedEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("event=order_confirmation_email_skipped reason=missing_recipient eventId={} orderId={}",
                    event.eventId(), event.orderId());
            return;
        }

        emailService.sendOrderConfirmation(
                event.recipientEmail(), event.orderId(), event.totalAmount(), event.itemCount());
        log.info("event=order_confirmation_email_sent eventId={} orderId={}", event.eventId(), event.orderId());
    }
}
