package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.config.CacheNames;
import com.ihsanerben.ecommerce_simulation_api.dto.internal.CachedProductPage;
import com.ihsanerben.ecommerce_simulation_api.dto.response.ProductResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.ProductMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.spec.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Cacheable(cacheNames = CacheNames.PRODUCT_SEARCH,
            key = "@cacheKeyFactory.productSearch(#categoryId, #search, #pageable)")
    public CachedProductPage searchProducts(Long categoryId, String search, Pageable pageable) {
        Specification<Product> specification = Specification.where(ProductSpecifications.hasCategoryId(categoryId))
                .and(ProductSpecifications.nameContains(search));
        Page<ProductResponse> products = productRepository.findAll(specification, pageable)
                .map(productMapper::toResponse);
        return new CachedProductPage(products.getContent(), products.getTotalElements());
    }

    @Cacheable(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }
}
