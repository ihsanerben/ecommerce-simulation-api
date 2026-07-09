package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.response.OrderResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Cart;
import com.ihsanerben.ecommerce_simulation_api.entity.CartItem;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Order;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderItem;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderStatus;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.EmptyCartException;
import com.ihsanerben.ecommerce_simulation_api.exception.InsufficientStockException;
import com.ihsanerben.ecommerce_simulation_api.exception.InvalidOrderStateException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CategoryMapper;
import com.ihsanerben.ecommerce_simulation_api.mapper.OrderMapper;
import com.ihsanerben.ecommerce_simulation_api.mapper.ProductMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.OrderRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, cartRepository, userRepository,
                new OrderMapper(new ProductMapper(new CategoryMapper())));
    }

    private Category sampleCategory() {
        return Category.builder().id(1L).name("Electronics").description("Gadgets").build();
    }

    private Product sampleProduct(long id, String name, String price, int stock) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .id(id)
                .name(name)
                .description("desc")
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(sampleCategory())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private User sampleUser(long id) {
        return User.builder()
                .id(id)
                .username("ihsan")
                .email("ihsan@example.com")
                .password("hashed")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Cart cartWith(User user, CartItem... items) {
        Cart cart = Cart.builder().id(1L).user(user).cartItems(new ArrayList<>()).build();
        for (CartItem item : items) {
            item.setCart(cart);
            cart.getCartItems().add(item);
        }
        return cart;
    }

    @Test
    void checkout_whenCartDoesNotExist_throwsEmptyCartException() {
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_whenCartIsEmpty_throwsEmptyCartException() {
        User user = sampleUser(1L);
        Cart cart = cartWith(user);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_whenAnyItemHasInsufficientStock_throwsInsufficientStockException_andSavesNothing() {
        User user = sampleUser(1L);
        Product plentiful = sampleProduct(1L, "Mouse", "20.00", 100);
        Product scarce = sampleProduct(2L, "Laptop", "1000.00", 1);
        Cart cart = cartWith(user,
                CartItem.builder().id(1L).product(plentiful).quantity(2).build(),
                CartItem.builder().id(2L).product(scarce).quantity(5).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(InsufficientStockException.class);

        // The all-or-nothing guarantee: even though "Mouse" alone had enough stock,
        // nothing should be persisted and nothing should be mutated because "Laptop" failed.
        verify(orderRepository, never()).save(any());
        assertThat(plentiful.getStockQuantity()).isEqualTo(100);
        assertThat(scarce.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void checkout_whenValid_createsOrderWithSnapshotPricesDecrementsStockAndClearsCart() {
        User user = sampleUser(1L);
        Product mouse = sampleProduct(1L, "Mouse", "20.00", 100);
        Product laptop = sampleProduct(2L, "Laptop", "1000.00", 5);
        Cart cart = cartWith(user,
                CartItem.builder().id(1L).product(mouse).quantity(2).build(),
                CartItem.builder().id(2L).product(laptop).quantity(1).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.checkout(1L);

        assertThat(mouse.getStockQuantity()).isEqualTo(98);
        assertThat(laptop.getStockQuantity()).isEqualTo(4);
        assertThat(cart.getCartItems()).isEmpty();

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("1040.00");
        assertThat(response.items()).hasSize(2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderItems().get(1).getUnitPrice()).isEqualByComparingTo("1000.00");
    }

    @Test
    void checkout_priceSnapshotIsIndependentOfLaterProductPriceChanges() {
        User user = sampleUser(1L);
        Product laptop = sampleProduct(2L, "Laptop", "1000.00", 5);
        Cart cart = cartWith(user, CartItem.builder().id(1L).product(laptop).quantity(1).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.checkout(1L);

        // Price changes on the product AFTER checkout must not affect the already-placed order.
        laptop.setPrice(new BigDecimal("1500.00"));

        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("1000.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void getOrderById_whenNotOwned_throwsResourceNotFoundException() {
        given(orderRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_whenOrderNotExists_throwsResourceNotFoundException() {
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.SHIPPED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_whenValid_updatesStatus() {
        User user = sampleUser(1L);
        Order order = Order.builder()
                .id(1L).user(user).orderItems(new ArrayList<>())
                .totalAmount(BigDecimal.TEN).status(OrderStatus.PENDING).createdAt(LocalDateTime.now())
                .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void cancelOrder_whenNotOwned_throwsResourceNotFoundException() {
        given(orderRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_throwsInvalidOrderStateException() {
        User user = sampleUser(1L);
        Order order = Order.builder()
                .id(1L).user(user).orderItems(new ArrayList<>())
                .totalAmount(BigDecimal.TEN).status(OrderStatus.CANCELLED).createdAt(LocalDateTime.now())
                .build();
        given(orderRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancelOrder_whenDelivered_throwsInvalidOrderStateException() {
        User user = sampleUser(1L);
        Order order = Order.builder()
                .id(1L).user(user).orderItems(new ArrayList<>())
                .totalAmount(BigDecimal.TEN).status(OrderStatus.DELIVERED).createdAt(LocalDateTime.now())
                .build();
        given(orderRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancelOrder_whenValid_restoresStockAndSetsCancelledStatus() {
        User user = sampleUser(1L);
        Product laptop = sampleProduct(2L, "Laptop", "1000.00", 3);
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = Order.builder()
                .id(1L).user(user).orderItems(orderItems)
                .totalAmount(new BigDecimal("2000.00")).status(OrderStatus.PENDING).createdAt(LocalDateTime.now())
                .build();
        orderItems.add(OrderItem.builder().id(1L).order(order).product(laptop).quantity(2).unitPrice(new BigDecimal("1000.00")).build());
        given(orderRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(order));

        OrderResponse response = orderService.cancelOrder(1L, 1L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(laptop.getStockQuantity()).isEqualTo(5);
    }
}
