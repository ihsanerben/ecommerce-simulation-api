package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.dto.request.AddCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CartResponse;
import com.ihsanerben.ecommerce_simulation_api.security.UserPrincipal;
import com.ihsanerben.ecommerce_simulation_api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "The logged-in user's own cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the cart", description = "If the user has no cart, an empty one is created automatically.")
    @ApiResponse(responseCode = "200", description = "Cart")
    @ApiResponse(responseCode = "401", description = "Authentication cookie is missing, expired, or revoked")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to the cart", description = "If the product is already in the cart, the quantity is added up.")
    @ApiResponse(responseCode = "201", description = "Product added to the cart")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "409", description = "Insufficient stock")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = cartService.addItem(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update the quantity of a cart item")
    @ApiResponse(responseCode = "200", description = "Quantity updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Cart item not found (or does not belong to you)")
    @ApiResponse(responseCode = "409", description = "Insufficient stock")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove a product from the cart")
    @ApiResponse(responseCode = "204", description = "Product removed from the cart")
    @ApiResponse(responseCode = "404", description = "Cart item not found (or does not belong to you)")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId) {
        cartService.removeItem(principal.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Empty the cart completely")
    @ApiResponse(responseCode = "204", description = "Cart emptied")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
