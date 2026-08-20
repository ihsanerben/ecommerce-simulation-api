package com.ihsanerben.ecommerce_simulation_api.order.stock;

import com.ihsanerben.ecommerce_simulation_api.order.stock.service.LowStockAlertService;
import com.ihsanerben.ecommerce_simulation_api.order.stock.entity.LowStockAlert;
import com.ihsanerben.ecommerce_simulation_api.order.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.order.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.order.stock.repository.LowStockAlertRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LowStockAlertServiceTest {

    private final LowStockAlertRepository repository = mock(LowStockAlertRepository.class);
    private final LowStockAlertService service = new LowStockAlertService(repository);

    @Test
    void recordAlertsPersistsOnlyProductsAtOrBelowThreshold() {
        UUID eventId = UUID.randomUUID();
        OrderCreatedEvent event = event(eventId, List.of(
                new OrderItemSnapshot(20L, "Keyboard", 1, new BigDecimal("125.00"), 5),
                new OrderItemSnapshot(21L, "Mouse", 1, new BigDecimal("50.00"), 6)));

        service.recordAlerts(event);

        ArgumentCaptor<LowStockAlert> captor = ArgumentCaptor.forClass(LowStockAlert.class);
        verify(repository).save(captor.capture());
        LowStockAlert alert = captor.getValue();
        assertThat(alert.getEventId()).isEqualTo(eventId);
        assertThat(alert.getOrderId()).isEqualTo(10L);
        assertThat(alert.getProductId()).isEqualTo(20L);
        assertThat(alert.getProductName()).isEqualTo("Keyboard");
        assertThat(alert.getRemainingStock()).isEqualTo(5);
    }

    @Test
    void recordAlertsSkipsAlreadyProcessedProduct() {
        UUID eventId = UUID.randomUUID();
        when(repository.existsByEventIdAndProductId(eventId, 20L)).thenReturn(true);
        OrderCreatedEvent event = event(eventId, List.of(
                new OrderItemSnapshot(20L, "Keyboard", 1, new BigDecimal("125.00"), 2)));

        service.recordAlerts(event);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private OrderCreatedEvent event(UUID eventId, List<OrderItemSnapshot> items) {
        return new OrderCreatedEvent(
                eventId,
                10L,
                2L,
                "buyer@example.com",
                new BigDecimal("125.00"),
                items.size(),
                items,
                Instant.now());
    }
}
