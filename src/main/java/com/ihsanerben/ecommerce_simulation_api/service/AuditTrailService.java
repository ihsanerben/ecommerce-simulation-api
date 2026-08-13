package com.ihsanerben.ecommerce_simulation_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.entity.AuditEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderApprovedEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCancelledEvent;
import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderCreatedEvent;
import com.ihsanerben.ecommerce_simulation_api.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditTrailService {

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_APPROVED = "ORDER_APPROVED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    private static final String ORDER = "ORDER";

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(OrderCreatedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalAmount", event.totalAmount());
        payload.put("itemCount", event.itemCount());
        payload.put("items", event.items());
        persist(event.eventId(), ORDER_CREATED, event.orderId(), event.userId(), event.occurredAt(), payload);
    }

    @Transactional
    public void record(OrderApprovedEvent event) {
        persist(event.eventId(), ORDER_APPROVED, event.orderId(), event.userId(), event.occurredAt(),
                Map.of("approved", true));
    }

    @Transactional
    public void record(OrderCancelledEvent event) {
        persist(event.eventId(), ORDER_CANCELLED, event.orderId(), event.userId(), event.occurredAt(),
                Map.of("status", "CANCELLED"));
    }

    private void persist(UUID eventId, String eventType, Long aggregateId, Long actorUserId,
            Instant occurredAt, Object payload) {
        if (auditEventRepository.existsByEventId(eventId)) {
            return;
        }

        auditEventRepository.save(AuditEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateType(ORDER)
                .aggregateId(aggregateId)
                .actorUserId(actorUserId)
                .occurredAt(occurredAt)
                .recordedAt(Instant.now())
                .payload(writePayload(payload))
                .build());
    }

    private String writePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Audit payload could not be serialized.", exception);
        }
    }
}
