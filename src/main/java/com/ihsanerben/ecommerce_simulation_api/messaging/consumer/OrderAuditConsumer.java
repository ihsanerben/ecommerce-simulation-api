package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAuditConsumer {

    private final AuditTrailService auditTrailService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-created-audit")
    public void consumeCreated(OrderCreatedEvent event) {
        auditTrailService.record(event);
        log.info("event=audit_recorded eventType=ORDER_CREATED eventId={} orderId={}",
                event.eventId(), event.orderId());
    }

    @KafkaListener(topics = KafkaTopics.ORDER_APPROVED, groupId = "order-approved-audit")
    public void consumeApproved(OrderApprovedEvent event) {
        auditTrailService.record(event);
        log.info("event=audit_recorded eventType=ORDER_APPROVED eventId={} orderId={}",
                event.eventId(), event.orderId());
    }
}
