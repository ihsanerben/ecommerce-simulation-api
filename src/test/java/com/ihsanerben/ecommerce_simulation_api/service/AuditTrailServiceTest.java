package com.ihsanerben.ecommerce_simulation_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.entity.AuditEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCancelledEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import com.ihsanerben.ecommerce_simulation_api.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
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

class AuditTrailServiceTest {

    private final AuditEventRepository auditEventRepository = mock(AuditEventRepository.class);
    private AuditTrailService auditTrailService;

    @BeforeEach
    void setUp() {
        auditTrailService = new AuditTrailService(auditEventRepository, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void recordCreatedPersistsSafeBusinessSnapshot() throws Exception {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-13T12:00:00Z");
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                10L,
                2L,
                "buyer@example.com",
                new BigDecimal("125.00"),
                1,
                List.of(new OrderItemSnapshot(20L, "Keyboard", 1, new BigDecimal("125.00"), 4)),
                occurredAt);

        auditTrailService.record(event);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getEventType()).isEqualTo(AuditTrailService.ORDER_CREATED);
        assertThat(saved.getAggregateType()).isEqualTo("ORDER");
        assertThat(saved.getAggregateId()).isEqualTo(10L);
        assertThat(saved.getActorUserId()).isEqualTo(2L);
        assertThat(saved.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(saved.getPayload()).contains("Keyboard", "125.00");
        assertThat(saved.getPayload()).doesNotContain("buyer@example.com");
    }

    @Test
    void recordSkipsAlreadyProcessedEvent() {
        UUID eventId = UUID.randomUUID();
        when(auditEventRepository.existsByEventId(eventId)).thenReturn(true);
        OrderApprovedEvent event = new OrderApprovedEvent(
                eventId, 10L, 2L, "buyer@example.com", Instant.now());

        auditTrailService.record(event);

        verify(auditEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void recordCancelledPersistsCancellationAudit() {
        UUID eventId = UUID.randomUUID();
        OrderCancelledEvent event = new OrderCancelledEvent(
                eventId, 10L, 2L, "buyer@example.com", Instant.parse("2026-08-13T12:00:00Z"));

        auditTrailService.record(event);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(captor.getValue().getEventType()).isEqualTo(AuditTrailService.ORDER_CANCELLED);
        assertThat(captor.getValue().getPayload()).contains("CANCELLED");
    }
}
