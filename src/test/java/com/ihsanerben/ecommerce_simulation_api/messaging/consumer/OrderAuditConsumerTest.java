package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.service.AuditTrailService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderAuditConsumerTest {

    private final AuditTrailService auditTrailService = mock(AuditTrailService.class);
    private final OrderAuditConsumer consumer = new OrderAuditConsumer(auditTrailService);

    @Test
    void createdEventIsDelegatedToAuditTrail() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                10L,
                2L,
                "buyer@example.com",
                new BigDecimal("125.00"),
                1,
                List.of(new OrderItemSnapshot("Keyboard", 1, new BigDecimal("125.00"))),
                Instant.now());

        consumer.consumeCreated(event);

        verify(auditTrailService).record(event);
    }

    @Test
    void approvedEventIsDelegatedToAuditTrail() {
        OrderApprovedEvent event = new OrderApprovedEvent(
                UUID.randomUUID(), 10L, 2L, "buyer@example.com", Instant.now());

        consumer.consumeApproved(event);

        verify(auditTrailService).record(event);
    }
}
