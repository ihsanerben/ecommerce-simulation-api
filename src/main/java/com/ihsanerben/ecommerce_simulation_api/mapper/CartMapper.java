package com.ihsanerben.ecommerce_simulation_api.mapper;

import com.ihsanerben.ecommerce_simulation_api.dto.response.CartItemResponse;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CartResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Cart;
import com.ihsanerben.ecommerce_simulation_api.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final ProductMapper productMapper;

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(item -> item.product().price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalPrice);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(item.getId(), productMapper.toResponse(item.getProduct()), item.getQuantity());
    }
}
