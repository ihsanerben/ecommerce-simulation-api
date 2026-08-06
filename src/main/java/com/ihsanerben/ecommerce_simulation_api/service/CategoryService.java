package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.config.CacheNames;
import com.ihsanerben.ecommerce_simulation_api.dto.request.CategoryRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CategoryResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.exception.DuplicateResourceException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CategoryMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = CacheNames.CATEGORY_BY_ID, key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.toResponse(findCategoryOrThrow(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CATEGORIES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CATEGORY_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        Category category = categoryMapper.toEntity(request);
        categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CATEGORIES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CATEGORY_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);

        boolean nameChanged = !category.getName().equals(request.name());
        if (nameChanged && categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        categoryMapper.updateEntity(category, request);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CATEGORIES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CATEGORY_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public void deleteCategory(Long id) {
        Category category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}
