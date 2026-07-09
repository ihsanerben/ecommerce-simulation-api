package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.AddCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CartResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Cart;
import com.ihsanerben.ecommerce_simulation_api.entity.CartItem;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.InsufficientStockException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CartMapper;
import com.ihsanerben.ecommerce_simulation_api.mapper.CategoryMapper;
import com.ihsanerben.ecommerce_simulation_api.mapper.ProductMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CartItemRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository, cartItemRepository, productRepository, userRepository,
                new CartMapper(new ProductMapper(new CategoryMapper())));
    }

    private Category sampleCategory() {
        return Category.builder().id(1L).name("Electronics").description("Gadgets").build();
    }

    private Product sampleProduct(long id, int stock) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .id(id)
                .name("Laptop")
                .description("A laptop")
                .price(new BigDecimal("999.99"))
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

    private Cart emptyCart(long id, User user) {
        return Cart.builder().id(id).user(user).cartItems(new ArrayList<>()).build();
    }

    @Test
    void getCart_whenCartExists_returnsIt() {
        User user = sampleUser(1L);
        Cart cart = emptyCart(5L, user);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.items()).isEmpty();
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCart_whenCartDoesNotExist_createsOne() {
        User user = sampleUser(1L);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(10L);
            return cart;
        });

        CartResponse response = cartService.getCart(1L);

        assertThat(response.id()).isEqualTo(10L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_whenProductNotExists_throwsResourceNotFoundException() {
        User user = sampleUser(1L);
        Cart cart = emptyCart(5L, user);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        AddCartItemRequest request = new AddCartItemRequest(99L, 1);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addItem_whenStockInsufficient_throwsInsufficientStockException() {
        User user = sampleUser(1L);
        Cart cart = emptyCart(5L, user);
        Product product = sampleProduct(2L, 3);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(productRepository.findById(2L)).willReturn(Optional.of(product));

        AddCartItemRequest request = new AddCartItemRequest(2L, 5);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addItem_whenNewProduct_addsCartItem() {
        User user = sampleUser(1L);
        Cart cart = emptyCart(5L, user);
        Product product = sampleProduct(2L, 10);
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(productRepository.findById(2L)).willReturn(Optional.of(product));

        AddCartItemRequest request = new AddCartItemRequest(2L, 3);

        CartResponse response = cartService.addItem(1L, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
    }

    @Test
    void addItem_whenProductAlreadyInCart_incrementsQuantity() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 10);
        Cart cart = emptyCart(5L, user);
        cart.getCartItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(productRepository.findById(2L)).willReturn(Optional.of(product));

        AddCartItemRequest request = new AddCartItemRequest(2L, 3);

        CartResponse response = cartService.addItem(1L, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void addItem_whenIncrementExceedsStock_throwsInsufficientStockException() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 4);
        Cart cart = emptyCart(5L, user);
        cart.getCartItems().add(CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(productRepository.findById(2L)).willReturn(Optional.of(product));

        AddCartItemRequest request = new AddCartItemRequest(2L, 3);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void updateItemQuantity_whenNotOwned_throwsResourceNotFoundException() {
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.empty());

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemQuantity_whenStockInsufficient_throwsInsufficientStockException() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 3);
        Cart cart = emptyCart(5L, user);
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.of(item));

        UpdateCartItemRequest request = new UpdateCartItemRequest(10);

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void updateItemQuantity_whenValid_updatesQuantity() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 10);
        Cart cart = emptyCart(5L, user);
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getCartItems().add(item);
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.of(item));

        UpdateCartItemRequest request = new UpdateCartItemRequest(7);

        CartResponse response = cartService.updateItemQuantity(1L, 1L, request);

        assertThat(response.items().get(0).quantity()).isEqualTo(7);
    }

    @Test
    void removeItem_whenNotOwned_throwsResourceNotFoundException() {
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_whenOwned_deletesItem() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 10);
        Cart cart = emptyCart(5L, user);
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.of(item));

        cartService.removeItem(1L, 1L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void clearCart_removesAllItems() {
        User user = sampleUser(1L);
        Product product = sampleProduct(2L, 10);
        Cart cart = emptyCart(5L, user);
        List<CartItem> items = cart.getCartItems();
        items.add(CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build());
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        cartService.clearCart(1L);

        assertThat(cart.getCartItems()).isEmpty();
    }
}
