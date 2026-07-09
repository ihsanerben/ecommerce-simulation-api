package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.response.OrderResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Cart;
import com.ihsanerben.ecommerce_simulation_api.entity.CartItem;
import com.ihsanerben.ecommerce_simulation_api.entity.Order;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderItem;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderStatus;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.EmptyCartException;
import com.ihsanerben.ecommerce_simulation_api.exception.InsufficientStockException;
import com.ihsanerben.ecommerce_simulation_api.exception.InvalidOrderStateException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.OrderMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.OrderRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse checkout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty, cannot checkout.");
        }

        // Fail fast: validate every line's stock BEFORE mutating anything,
        // so a single insufficient-stock item can never leave a partial order behind.
        for (CartItem cartItem : cart.getCartItems()) {
            ensureSufficientStock(cartItem.getProduct(), cartItem.getQuantity());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.getOrderItems().add(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);
        cart.getCartItems().clear();

        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getOrders(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long userId, Long orderId) {
        return orderMapper.toResponse(findOwnedOrderOrThrow(userId, orderId));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(newStatus);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = findOwnedOrderOrThrow(userId, orderId);

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    "Order in status '%s' cannot be cancelled.".formatted(order.getStatus()));
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }

    private Order findOwnedOrderOrThrow(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    private void ensureSufficientStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '%s': requested %d, available %d"
                            .formatted(product.getName(), requestedQuantity, product.getStockQuantity()));
        }
    }
}
