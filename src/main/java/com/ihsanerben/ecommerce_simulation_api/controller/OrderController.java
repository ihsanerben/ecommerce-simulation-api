package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateOrderStatusRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.OrderResponse;
import com.ihsanerben.ecommerce_simulation_api.security.UserPrincipal;
import com.ihsanerben.ecommerce_simulation_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Sipariş oluşturma (checkout), listeleme ve iptal")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Checkout", description = "Kullanıcının sepetinden sipariş oluşturur: stok kontrolü, "
            + "fiyat snapshot'ı, stok düşürme ve sepeti boşaltma tek bir transaction içinde yürütülür.")
    @ApiResponse(responseCode = "201", description = "Sipariş oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Sepet boş")
    @ApiResponse(responseCode = "409", description = "Bir veya birden fazla ürün için yetersiz stok")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserPrincipal principal) {
        OrderResponse response = orderService.checkout(principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Siparişlerimi listele")
    @ApiResponse(responseCode = "200", description = "Sipariş listesi")
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrders(principal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sipariş detayı", description = "Sadece kendi siparişinize erişebilirsiniz.")
    @ApiResponse(responseCode = "200", description = "Sipariş bulundu")
    @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı (veya size ait değil)")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(principal.getId(), id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sipariş durumunu güncelle", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "200", description = "Durum güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz durum değeri")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Siparişi iptal et", description = "Sadece kendi siparişiniz; iptal edilen siparişin stoğu geri iade edilir.")
    @ApiResponse(responseCode = "200", description = "Sipariş iptal edildi")
    @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı (veya size ait değil)")
    @ApiResponse(responseCode = "409", description = "Bu durumdaki bir sipariş iptal edilemez")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(principal.getId(), id));
    }
}
