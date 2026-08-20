package com.ihsanerben.ecommerce_simulation_api.order.stock.messaging;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import com.ihsanerben.ecommerce_simulation_api.order.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.order.stock.service.LowStockAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertConsumer {

    private final LowStockAlertService lowStockAlertService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-created-low-stock")
    public void consume(OrderCreatedEvent event) {
        lowStockAlertService.recordAlerts(event);
        log.info("event=low_stock_evaluated eventId={} orderId={}", event.eventId(), event.orderId());
    }
}
