package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.websocket.LiveNotificationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderCreatedEventConsumerTest {

    @Test
    void shouldBroadcastLiveNotificationWhenOrderEventIsConsumed() {
        LiveNotificationService notificationService = mock(LiveNotificationService.class);
        OrderCreatedEventConsumer consumer = new OrderCreatedEventConsumer(notificationService);
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                1L,
                2L,
                "buyer@example.com",
                BigDecimal.TEN,
                1,
                java.util.List.of(new OrderItemSnapshot("Mouse", 1, BigDecimal.TEN)),
                Instant.now()
        );

        consumer.consume(event);

        verify(notificationService).broadcastOrderCreated();
    }
}
