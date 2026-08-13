package com.ihsanerben.ecommerce_simulation_api.websocket;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderCreatedNotificationListenerTest {

    @Test
    void shouldBroadcastLiveNotificationWhenOrderIsCreated() {
        LiveNotificationService notificationService = mock(LiveNotificationService.class);
        OrderCreatedNotificationListener listener = new OrderCreatedNotificationListener(notificationService);
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

        listener.onOrderCreated(event);

        verify(notificationService).broadcastOrderCreated();
    }
}
