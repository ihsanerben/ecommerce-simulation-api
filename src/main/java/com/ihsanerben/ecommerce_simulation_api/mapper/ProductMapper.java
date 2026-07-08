package com.ihsanerben.ecommerce_simulation_api.mapper;

import com.ihsanerben.ecommerce_simulation_api.dto.request.ProductRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.ProductResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public Product toEntity(ProductRequest request, Category category) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateEntity(Product product, ProductRequest request, Category category) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                categoryMapper.toResponse(product.getCategory()),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
