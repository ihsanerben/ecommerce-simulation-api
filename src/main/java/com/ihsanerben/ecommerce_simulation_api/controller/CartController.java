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
@Tag(name = "Cart", description = "Giriş yapmış kullanıcının kendi sepeti")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Sepeti getir", description = "Kullanıcının sepeti yoksa otomatik olarak boş bir sepet oluşturulur.")
    @ApiResponse(responseCode = "200", description = "Sepet")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Sepete ürün ekle", description = "Ürün zaten sepetteyse miktar toplanır.")
    @ApiResponse(responseCode = "201", description = "Ürün sepete eklendi")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    @ApiResponse(responseCode = "409", description = "Yetersiz stok")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = cartService.addItem(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Sepetteki ürün adedini güncelle")
    @ApiResponse(responseCode = "200", description = "Adet güncellendi")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "404", description = "Sepet kalemi bulunamadı (veya size ait değil)")
    @ApiResponse(responseCode = "409", description = "Yetersiz stok")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Sepetten ürün çıkar")
    @ApiResponse(responseCode = "204", description = "Ürün sepetten çıkarıldı")
    @ApiResponse(responseCode = "404", description = "Sepet kalemi bulunamadı (veya size ait değil)")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long itemId) {
        cartService.removeItem(principal.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Sepeti tamamen boşalt")
    @ApiResponse(responseCode = "204", description = "Sepet boşaltıldı")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
