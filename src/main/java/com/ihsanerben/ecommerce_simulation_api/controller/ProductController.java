package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.dto.request.ProductRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.ProductResponse;
import com.ihsanerben.ecommerce_simulation_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Ürün yönetimi — okuma herkese açık, yazma sadece ADMIN")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Ürünleri listele", description = "Sayfalama, kategoriye göre filtreleme ve isme göre arama destekler.")
    @ApiResponse(responseCode = "200", description = "Sayfalanmış ürün listesi")
    public ResponseEntity<PagedModel<ProductResponse>> getProducts(
            @Parameter(description = "Kategoriye göre filtrele") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Ürün adına göre ara (büyük/küçük harf duyarsız)") @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<ProductResponse> page = productService.searchProducts(categoryId, search, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı")
    @ApiResponse(responseCode = "200", description = "Ürün bulundu")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün ekle", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "201", description = "Ürün oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün güncelle", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "200", description = "Ürün güncellendi")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Ürün veya kategori bulunamadı")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün sil", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "204", description = "Ürün silindi")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
