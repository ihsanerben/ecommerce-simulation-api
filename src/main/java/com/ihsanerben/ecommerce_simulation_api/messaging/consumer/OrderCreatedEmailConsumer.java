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
public class OrderCreatedEmailConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-confirmation-email")
    public void consumeConfirmation(OrderCreatedEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("event=order_confirmation_email_skipped reason=missing_recipient eventId={} orderId={}",
                    event.eventId(), event.orderId());
            return;
        }

        emailService.sendOrderConfirmation(
                event.recipientEmail(), event.orderId(), event.totalAmount(), event.itemCount());
        log.info("event=order_confirmation_email_sent eventId={} orderId={}", event.eventId(), event.orderId());
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-invoice-email")
    public void consumeInvoice(OrderCreatedEvent event) {
        if (event.recipientEmail() == null || event.recipientEmail().isBlank() || event.items() == null) {
            log.warn("event=order_invoice_email_skipped reason=incomplete_legacy_event eventId={} orderId={}",
                    event.eventId(), event.orderId());
            return;
        }

        emailService.sendInvoice(event.recipientEmail(), event.orderId(), event.totalAmount(), event.items());
        log.info("event=order_invoice_email_sent eventId={} orderId={}", event.eventId(), event.orderId());
    }
}
