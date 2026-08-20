package com.ihsanerben.ecommerce_simulation_api.order.dto.request;

import com.ihsanerben.ecommerce_simulation_api.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
