package com.ihsanerben.ecommerce_simulation_api.websocket;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedNotificationListener {

    private final LiveNotificationService liveNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("event=order_created_notification eventId={} orderId={} userId={} itemCount={}",
                event.eventId(), event.orderId(), event.userId(), event.itemCount());
        liveNotificationService.broadcastOrderCreated();
    }
}
