package com.ihsanerben.ecommerce_simulation_api.messaging.consumer;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCancelledEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.service.EmailService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderEmailConsumersTest {

    private final EmailService emailService = mock(EmailService.class);

    @Test
    void confirmationConsumerSendsOrderConfirmation() {
        OrderCreatedEvent event = createdEvent();

        new OrderCreatedEmailConsumer(emailService).consumeConfirmation(event);

        verify(emailService).sendOrderConfirmation("buyer@example.com", 10L, new BigDecimal("125.00"), 1);
    }

    @Test
    void invoiceConsumerSendsInvoice() {
        OrderCreatedEvent event = createdEvent();

        new OrderCreatedEmailConsumer(emailService).consumeInvoice(event);

        verify(emailService).sendInvoice("buyer@example.com", 10L, new BigDecimal("125.00"), event.items());
    }

    @Test
    void approvedConsumerSendsPreparingEmail() {
        OrderApprovedEvent event = new OrderApprovedEvent(
                UUID.randomUUID(), 10L, 2L, "buyer@example.com", Instant.now());

        new OrderApprovedEmailConsumer(emailService).consume(event);

        verify(emailService).sendOrderPreparing("buyer@example.com", 10L);
    }

    @Test
    void cancelledConsumerSendsCancellationEmail() {
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(), 10L, 2L, "buyer@example.com", Instant.now());

        new OrderCancelledEmailConsumer(emailService).consume(event);

        verify(emailService).sendOrderCancelled("buyer@example.com", 10L);
    }

    @Test
    void cancelledConsumerSkipsLegacyEventWithoutRecipient() {
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(), 10L, 2L, null, Instant.now());

        new OrderCancelledEmailConsumer(emailService).consume(event);

        verify(emailService, never()).sendOrderCancelled(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void emailConsumersSkipLegacyEventWithoutRecipientAndItems() {
        OrderCreatedEvent legacyEvent = new OrderCreatedEvent(
                UUID.randomUUID(), 10L, 2L, null, new BigDecimal("125.00"), 1, null, Instant.now());

        OrderCreatedEmailConsumer consumer = new OrderCreatedEmailConsumer(emailService);
        consumer.consumeConfirmation(legacyEvent);
        consumer.consumeInvoice(legacyEvent);

        verify(emailService, never()).sendOrderConfirmation(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(emailService, never()).sendInvoice(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private OrderCreatedEvent createdEvent() {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                10L,
                2L,
                "buyer@example.com",
                new BigDecimal("125.00"),
                1,
                List.of(new OrderItemSnapshot(20L, "Keyboard", 1, new BigDecimal("125.00"), 4)),
                Instant.now());
    }
}
