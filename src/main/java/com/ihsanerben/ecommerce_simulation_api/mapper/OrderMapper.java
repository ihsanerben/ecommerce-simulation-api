package com.ihsanerben.ecommerce_simulation_api.mapper;

import com.ihsanerben.ecommerce_simulation_api.dto.response.OrderItemResponse;
import com.ihsanerben.ecommerce_simulation_api.dto.response.OrderResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Order;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductMapper productMapper;

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(order.getId(), items, order.getTotalAmount(), order.getStatus(), order.isApproved(),
                order.getCreatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(), productMapper.toResponse(item.getProduct()), item.getQuantity(), item.getUnitPrice(), subtotal);
    }
}
