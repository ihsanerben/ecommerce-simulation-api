package com.ihsanerben.ecommerce_simulation_api.order.audit.repository;

import com.ihsanerben.ecommerce_simulation_api.order.audit.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(UUID eventId);
}
