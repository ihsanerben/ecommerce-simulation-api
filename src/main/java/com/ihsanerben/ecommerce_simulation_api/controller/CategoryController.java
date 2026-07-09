package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.dto.request.CategoryRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CategoryResponse;
import com.ihsanerben.ecommerce_simulation_api.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Kategori yönetimi — okuma herkese açık, yazma sadece ADMIN")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Tüm kategorileri listele")
    @ApiResponse(responseCode = "200", description = "Kategori listesi")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kategori detayı")
    @ApiResponse(responseCode = "200", description = "Kategori bulundu")
    @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Kategori oluştur", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "201", description = "Kategori oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "409", description = "Bu isimde bir kategori zaten var")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Kategori güncelle", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "200", description = "Kategori güncellendi")
    @ApiResponse(responseCode = "400", description = "Doğrulama hatası")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
    @ApiResponse(responseCode = "409", description = "Bu isimde başka bir kategori zaten var")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Kategori sil", description = "Sadece ADMIN")
    @ApiResponse(responseCode = "204", description = "Kategori silindi")
    @ApiResponse(responseCode = "403", description = "ADMIN yetkisi gerekli")
    @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
