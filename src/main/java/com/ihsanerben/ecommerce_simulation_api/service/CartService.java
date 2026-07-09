package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.AddCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CartResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Cart;
import com.ihsanerben.ecommerce_simulation_api.entity.CartItem;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.InsufficientStockException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CartMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CartItemRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getCart(Long userId) {
        return cartMapper.toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = findProductOrThrow(request.productId());

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int newQuantity = existingItem.map(CartItem::getQuantity).orElse(0) + request.quantity();
        ensureSufficientStock(product, newQuantity);

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findOwnedItemOrThrow(userId, itemId);
        ensureSufficientStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        return cartMapper.toResponse(item.getCart());
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = findOwnedItemOrThrow(userId, itemId);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Cart cart = Cart.builder().user(user).build();
        return cartRepository.save(cart);
    }

    private CartItem findOwnedItemOrThrow(Long userId, Long itemId) {
        return cartItemRepository.findByIdAndCart_User_Id(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private void ensureSufficientStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '%s': requested %d, available %d"
                            .formatted(product.getName(), requestedQuantity, product.getStockQuantity()));
        }
    }
}
