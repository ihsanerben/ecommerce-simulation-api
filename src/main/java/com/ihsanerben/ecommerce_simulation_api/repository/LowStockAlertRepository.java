package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.entity.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {

    boolean existsByEventIdAndProductId(UUID eventId, Long productId);
}
