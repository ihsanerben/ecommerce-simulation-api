package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApprovedEmailConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = KafkaTopics.ORDER_APPROVED, groupId = "order-approved-email")
    public void consume(OrderApprovedEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("event=order_preparing_email_skipped reason=missing_recipient eventId={} orderId={}",
                    event.eventId(), event.orderId());
            return;
        }

        emailService.sendOrderPreparing(event.recipientEmail(), event.orderId());
        log.info("event=order_preparing_email_sent eventId={} orderId={}", event.eventId(), event.orderId());
    }
}
