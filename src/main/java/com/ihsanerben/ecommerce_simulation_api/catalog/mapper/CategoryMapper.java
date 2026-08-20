package com.ihsanerben.ecommerce_simulation_api.catalog.mapper;

import com.ihsanerben.ecommerce_simulation_api.catalog.dto.request.CategoryRequest;
import com.ihsanerben.ecommerce_simulation_api.catalog.dto.response.CategoryResponse;
import com.ihsanerben.ecommerce_simulation_api.catalog.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
