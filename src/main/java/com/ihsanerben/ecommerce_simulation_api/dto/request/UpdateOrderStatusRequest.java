package com.ihsanerben.ecommerce_simulation_api.dto.request;

import com.ihsanerben.ecommerce_simulation_api.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
