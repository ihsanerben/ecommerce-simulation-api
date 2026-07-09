package com.ihsanerben.ecommerce_simulation_api.dto.response;

import com.ihsanerben.ecommerce_simulation_api.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
