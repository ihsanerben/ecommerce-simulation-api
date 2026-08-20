package com.ihsanerben.ecommerce_simulation_api.catalog.service;

import com.ihsanerben.ecommerce_simulation_api.config.CacheNames;
import com.ihsanerben.ecommerce_simulation_api.catalog.dto.request.ProductRequest;
import com.ihsanerben.ecommerce_simulation_api.catalog.dto.response.ProductResponse;
import com.ihsanerben.ecommerce_simulation_api.catalog.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.catalog.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.catalog.mapper.ProductMapper;
import com.ihsanerben.ecommerce_simulation_api.catalog.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductQueryService productQueryService;

    public Page<ProductResponse> searchProducts(Long categoryId, String search, Pageable pageable) {
        var cachedPage = productQueryService.searchProducts(categoryId, search, pageable);
        return new PageImpl<>(cachedPage.content(), pageable, cachedPage.totalElements());
    }

    public ProductResponse getProductById(Long id) {
        return productQueryService.getProductById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public ProductResponse createProduct(ProductRequest request) {
        Category category = findCategoryOrThrow(request.categoryId());
        Product product = productMapper.toEntity(request, category);
        productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        Category category = findCategoryOrThrow(request.categoryId());
        productMapper.updateEntity(product, request, category);
        return productMapper.toResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }
}
