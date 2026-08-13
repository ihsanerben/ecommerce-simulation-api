package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.LowStockAlert;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.repository.LowStockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LowStockAlertService {

    static final int LOW_STOCK_THRESHOLD = 5;

    private final LowStockAlertRepository lowStockAlertRepository;

    @Transactional
    public void recordAlerts(OrderCreatedEvent event) {
        if (event.items() == null) {
            return;
        }

        event.items().stream()
                .filter(item -> item.productId() != null)
                .filter(item -> item.remainingStock() <= LOW_STOCK_THRESHOLD)
                .filter(item -> !alreadyRecorded(event, item))
                .map(item -> toAlert(event, item))
                .forEach(lowStockAlertRepository::save);
    }

    private boolean alreadyRecorded(OrderCreatedEvent event, OrderItemSnapshot item) {
        return lowStockAlertRepository.existsByEventIdAndProductId(event.eventId(), item.productId());
    }

    private LowStockAlert toAlert(OrderCreatedEvent event, OrderItemSnapshot item) {
        return LowStockAlert.builder()
                .eventId(event.eventId())
                .orderId(event.orderId())
                .productId(item.productId())
                .productName(item.productName())
                .remainingStock(item.remainingStock())
                .createdAt(Instant.now())
                .build();
    }
}
