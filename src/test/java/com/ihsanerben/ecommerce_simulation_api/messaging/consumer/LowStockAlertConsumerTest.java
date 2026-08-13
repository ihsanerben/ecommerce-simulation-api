package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.service.LowStockAlertService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LowStockAlertConsumerTest {

    @Test
    void consumeDelegatesOrderCreatedEvent() {
        LowStockAlertService service = mock(LowStockAlertService.class);
        LowStockAlertConsumer consumer = new LowStockAlertConsumer(service);
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                10L,
                2L,
                "buyer@example.com",
                new BigDecimal("125.00"),
                0,
                List.of(),
                Instant.now());

        consumer.consume(event);

        verify(service).recordAlerts(event);
    }
}
